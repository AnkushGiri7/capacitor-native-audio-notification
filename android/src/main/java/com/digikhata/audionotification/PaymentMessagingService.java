package com.digikhata.audionotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentMessagingService extends FirebaseMessagingService {
    private static final String TAG = "PaymentMessagingService";
    private static final AtomicInteger NOTIFICATION_ID_GENERATOR = new AtomicInteger(1000);
    private static final String CHANNEL_ID = NotificationChannelHelper.DEFAULT_CHANNEL_ID;
    private PendingIntent ackPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            NativeAudioNotification.getInstance(this); // Initialize singleton
            startForegroundServiceNotification();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            NativeAudioNotification.destroyInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage(), e);
        }
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("type") && "payment".equals(data.get("type"))) {
            handlePaymentNotification(data, remoteMessage.getNotification());
        } else if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), data);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token received (partial): " + token.substring(0, Math.min(token.length(), 10)) + "...");
        sendTokenToServer(token);
    }

    private void handlePaymentNotification(Map<String, String> data, RemoteMessage.Notification notification) {
        String title = notification != null ? notification.getTitle() : "Payment Received";
        String body = notification != null ? notification.getBody() : "You have received a payment";
        String senderName = validateInput(data.get("senderName"), "Unknown");
        String amount = validateInput(data.get("amount"), "0");
        String currency = validateInput(data.get("currency"), "₹");
        String transactionId = validateInput(data.get("transactionId"), "");
        String imageUrl = data.get("imageUrl");

        wakeUpScreen();
        playTtsNotification(amount, currency, senderName);
        showPaymentNotification(title, body, senderName, amount, currency, transactionId, imageUrl);
    }

    private void playTtsNotification(String amount, String currency, String senderName) {
        try {
            NativeAudioNotification audioNotification = NativeAudioNotification.getInstance(this);
            if (audioNotification != null) {
                String audioText = currency + " " + amount + " received from " + senderName + " on " + audioNotification.getMerchantBusiness();
                audioNotification.playTtsOnly(audioText);
            } else {
                Log.w(TAG, "audioNotification not initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing TTS notification: " + e.getMessage(), e);
        }
    }

    private void showPaymentNotification(String title, String body, String senderName, String amount, String currency, String transactionId, String imageUrl) {
        try {
            Intent ackIntent = new Intent(this, AcknowledgeReceiver.class);
            ackIntent.putExtra("notificationId", transactionId.hashCode());
            ackPendingIntent = PendingIntent.getBroadcast(this, transactionId.hashCode(), ackIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent intent = new Intent(this, getMainActivityClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("transactionId", transactionId);
            intent.putExtra("senderName", senderName);
            intent.putExtra("amount", amount);
            intent.putExtra("currency", currency);
            intent.putExtra("notificationTapped", true);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID_GENERATOR.getAndIncrement(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setFullScreenIntent(pendingIntent, true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(false)
                    .setTicker(title)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVibrate(new long[]{0, 300, 100, 300, 100, 300, 100, 600})
                    .setLights(Color.GREEN, 1000, 1000)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis());

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                    .bigText("Payment of " + currency + amount + " received from " + senderName)
                    .setBigContentTitle("💰 " + title);
            builder.setStyle(bigTextStyle);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                InputStream in = new URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                builder.setLargeIcon(bitmap);
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null)
                        .setBigContentTitle("💰 " + title)
                        .setSummaryText("Payment of " + currency + amount + " received from " + senderName);
                builder.setStyle(bigPictureStyle);
            }

            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Acknowledge", ackPendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            int notificationId = transactionId.hashCode();
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Payment notification shown successfully with ID: " + notificationId);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show notification: permission denied", e);
        } catch (Exception e) {
            Log.e(TAG, "Error showing payment notification: " + e.getMessage(), e);
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        if (title == null || body == null) return;

        try {
            Intent intent = new Intent(this, getMainActivityClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID_GENERATOR.getAndIncrement(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID_GENERATOR.getAndIncrement(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show general notification", e);
        } catch (Exception e) {
            Log.e(TAG, "Error showing general notification: " + e.getMessage(), e);
        }
    }

    private void wakeUpScreen() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && !powerManager.isInteractive()) {
                @SuppressWarnings("deprecation")
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "PaymentNotification:WakeLock"
                );
                wakeLock.acquire(3000L);
                wakeLock.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error waking up screen: " + e.getMessage(), e);
        }
    }

    private void sendTokenToServer(String token) {
        Log.i(TAG, "Sending FCM token to server (partial): " + token.substring(0, Math.min(token.length(), 10)) + "...");
    }

    private Class<?> getMainActivityClass() {
        try {
            return Class.forName(getPackageName() + ".MainActivity");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MainActivity class not found", e);
            return null;
        }
    }

    private String validateInput(String input, String defaultValue) {
        return (input == null || input.trim().isEmpty()) ? defaultValue : input;
    }

    private void startForegroundServiceNotification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null && nm.getNotificationChannel(NotificationChannelHelper.DEFAULT_CHANNEL_ID) == null) {
                    Log.w(TAG, "Channel not found, creating fallback");
                    NotificationChannel channel = new NotificationChannel(
                            NotificationChannelHelper.DEFAULT_CHANNEL_ID, "Payment Service", NotificationManager.IMPORTANCE_LOW);
                    nm.createNotificationChannel(channel);
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelHelper.DEFAULT_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Payment Service Running")
                        .setContentText("Listening for payment notifications")
                        .setPriority(NotificationCompat.PRIORITY_LOW);

                startForeground(1001, builder.build());
                Log.d(TAG, "Foreground service started");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception starting foreground service: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service: " + e.getMessage(), e);
        }
    }

    public String getMerchantBusiness() {
        NativeAudioNotification audioNotification = NativeAudioNotification.getInstance(this);
        return audioNotification != null ? audioNotification.getMerchantBusiness() : "Your Business";
    }
}