package com.digikhata.audionotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class NativeAudioNotification implements TextToSpeech.OnInitListener {
    private static final String TAG = "NativeAudioNotification";
    private static final String CHANNEL_ID = NotificationChannelHelper.DEFAULT_CHANNEL_ID;
    private static NativeAudioNotification instance;
    private Context context;
    private TextToSpeech tts;
    private String merchantBusiness = "Your Business";
    private PowerManager.WakeLock wakeLock;

    private NativeAudioNotification(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
        try {
            tts = new TextToSpeech(this.context, this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing NativeAudioNotification: " + e.getMessage(), e);
        }
    }

    public static synchronized NativeAudioNotification getInstance(Context context) {
        if (instance == null) {
            instance = new NativeAudioNotification(context);
        }
        return instance;
    }

    public static synchronized void destroyInstance() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }

    @Override
    public void onInit(int status) {
        try {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS language not supported, falling back to default");
                    tts.setLanguage(Locale.getDefault());
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in TTS onInit: " + e.getMessage(), e);
        }
    }

    public void setMerchantInfo(String businessName) {
        try {
            if (businessName != null && !businessName.trim().isEmpty()) {
                this.merchantBusiness = businessName;
                Log.d(TAG, "Merchant business set to: " + businessName);
            } else {
                Log.w(TAG, "Invalid or empty business name provided, using default");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting merchant info: " + e.getMessage(), e);
        }
    }

    public void playPaymentNotification(String amount, String currency, String customerName, String transactionId, long timestamp) {
        try {
            acquireWakeLock();
            String audioText = currency + " " + amount + " received from " + (customerName != null ? customerName : "customer") + " on " + merchantBusiness;
            playTtsOnly(audioText);
            showLockScreenNotification(amount, currency, customerName, transactionId, timestamp);
        } catch (Exception e) {
            Log.e(TAG, "Error playing payment notification: " + e.getMessage(), e);
        }
    }

    public void playTtsOnly(String text) {
        try {
            if (tts != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                tts.setAudioAttributes(audioAttributes);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "payment_utterance_" + System.currentTimeMillis());
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) { Log.d(TAG, "TTS started for utterance: " + utteranceId); }
                    @Override
                    public void onDone(String utteranceId) { Log.d(TAG, "TTS completed for utterance: " + utteranceId); }
                    @Override
                    public void onError(String utteranceId) { Log.e(TAG, "TTS error for utterance: " + utteranceId); }
                });
            } else {
                Log.w(TAG, "TTS not initialized, skipping audio");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing TTS: " + e.getMessage(), e);
        }
    }

    private void acquireWakeLock() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "PaymentNotification::WakeLock");
                if (wakeLock != null) {
                    wakeLock.acquire(10000);
                    Log.d(TAG, "Wake lock acquired");
                } else {
                    Log.w(TAG, "Failed to create wake lock");
                }
            } else {
                Log.w(TAG, "PowerManager service not available");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception acquiring wake lock: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error acquiring wake lock: " + e.getMessage(), e);
        }
    }

    private void showLockScreenNotification(String amount, String currency, String customerName, String transactionId, long timestamp) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                Intent intent = new Intent(context, Class.forName(context.getPackageName() + ".MainActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("transactionId", transactionId);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, transactionId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Payment Received")
                        .setContentText(currency + " " + amount + " from " + (customerName != null ? customerName : "customer"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(pendingIntent, true)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Acknowledge", getAcknowledgeIntent(transactionId.hashCode()))
                        .addAction(android.R.drawable.ic_menu_info_details, "View Details", pendingIntent)
                        .setAutoCancel(false)
                        .setWhen(timestamp)
                        .setShowWhen(true);

                nm.notify(transactionId.hashCode(), builder.build());
                Log.d(TAG, "Lock screen notification shown with ID: " + transactionId.hashCode());
            } else {
                Log.w(TAG, "NotificationManager not available");
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MainActivity class not found: " + e.getMessage(), e);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception showing notification: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error showing lock screen notification: " + e.getMessage(), e);
        }
    }

    private PendingIntent getAcknowledgeIntent(int notificationId) {
        try {
            Intent ackIntent = new Intent(context, AcknowledgeReceiver.class);
            ackIntent.putExtra("notificationId", notificationId);
            return PendingIntent.getBroadcast(context, notificationId, ackIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } catch (Exception e) {
            Log.e(TAG, "Error creating acknowledge intent: " + e.getMessage(), e);
            return null;
        }
    }

    public void cleanup() {
        try {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts = null;
                Log.d(TAG, "TTS cleaned up");
            }
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
                Log.d(TAG, "Wake lock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage(), e);
        }
    }

    public String getMerchantBusiness() {
        return merchantBusiness != null ? merchantBusiness : "Your Business";
    }

    public void stopTts() {
        try {
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
                Log.d(TAG, "TTS stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping TTS: " + e.getMessage(), e);
        }
    }
}