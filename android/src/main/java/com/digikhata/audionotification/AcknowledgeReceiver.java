package com.digikhata.audionotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;

public class AcknowledgeReceiver extends BroadcastReceiver {
    private static final String TAG = "AcknowledgeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Acknowledge action received");

        try {
            // Extract notification ID from intent
            int notificationId = intent.getIntExtra("notificationId", -1);
            if (notificationId == -1) {
                Log.w(TAG, "Invalid or missing notificationId in intent");
                return;
            }

            // Stop TTS
            NativeAudioNotification audioNotification = new NativeAudioNotification(context);
            audioNotification.stopTts();
            Log.d(TAG, "TTS stopped for notification ID: " + notificationId);

            // Dismiss the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);
            Log.d(TAG, "Notification dismissed with ID: " + notificationId);

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception during acknowledgment: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error processing acknowledgment: " + e.getMessage(), e);
        }
    }
}