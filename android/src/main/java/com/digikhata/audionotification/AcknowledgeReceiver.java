package com.digikhata.audionotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;
import java.util.Arrays;

public class AcknowledgeReceiver extends BroadcastReceiver {
    private static final String TAG = "AcknowledgeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Acknowledge action received");

        try {
            int notificationId = intent.getIntExtra("notificationId", -1);
            if (notificationId == -1) {
                Log.w(TAG, "Invalid or missing notificationId in intent");
                return;
            }

            NativeAudioNotification audioNotification = NativeAudioNotification.getInstance(context);
            if (audioNotification != null) {
                audioNotification.stopTts();
                Log.d(TAG, "TTS stopped for notification ID: " + notificationId);
            } else {
                Log.w(TAG, "NativeAudioNotification instance not available");
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
            boolean notificationExists = Arrays.stream(activeNotifications)
                    .anyMatch(n -> n.getId() == notificationId);
            if (notificationExists) {
                notificationManager.cancel(notificationId);
                Log.d(TAG, "Notification dismissed with ID: " + notificationId);
            } else {
                Log.w(TAG, "Notification with ID " + notificationId + " not found");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception during acknowledgment: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error processing acknowledgment: " + e.getMessage(), e);
        }
    }
}