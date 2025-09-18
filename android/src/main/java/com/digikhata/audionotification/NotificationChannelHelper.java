package com.digikhata.audionotification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

public class NotificationChannelHelper {
    private static final String TAG = "NotificationChannelHelper";
    public static final String DEFAULT_CHANNEL_ID = "payment_channel";
    public static final String DEFAULT_GROUP_ID = "payment_group";
    public static final String DEFAULT_CHANNEL_NAME = "Payment Alerts";
    public static final String DEFAULT_CHANNEL_DESCRIPTION = "Critical payment notifications";
    public static final String DEFAULT_GROUP_NAME = "Payment Notifications";

    /**
     * Creates a notification channel and group for payment alerts.
     * @param context The application context.
     * @param channelId The ID of the notification channel (defaults to DEFAULT_CHANNEL_ID if null/empty).
     * @param channelName The name of the notification channel (defaults to DEFAULT_CHANNEL_NAME if null/empty).
     * @param channelDescription The description of the notification channel (defaults to DEFAULT_CHANNEL_DESCRIPTION if null/empty).
     * @param groupId The ID of the notification channel group (defaults to DEFAULT_GROUP_ID if null/empty).
     * @param groupName The name of the notification channel group (defaults to DEFAULT_GROUP_NAME if null/empty).
     * @throws SecurityException if the app lacks permission to create notification channels.
     */
    public static void createPaymentNotificationChannel(
            Context context,
            String channelId,
            String channelName,
            String channelDescription,
            String groupId,
            String groupName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.d(TAG, "Skipping channel creation for API < 26");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null, cannot create channel");
            return;
        }

        try {
            channelId = channelId != null && !channelId.trim().isEmpty() ? channelId : DEFAULT_CHANNEL_ID;
            channelName = channelName != null && !channelName.trim().isEmpty() ? channelName : DEFAULT_CHANNEL_NAME;
            channelDescription = channelDescription != null && !channelDescription.trim().isEmpty() ? channelDescription : DEFAULT_CHANNEL_DESCRIPTION;
            groupId = groupId != null && !groupId.trim().isEmpty() ? groupId : DEFAULT_GROUP_ID;
            groupName = groupName != null && !groupName.trim().isEmpty() ? groupName : DEFAULT_GROUP_NAME;

            NotificationChannelGroup group = notificationManager.getNotificationChannelGroup(groupId);
            if (group == null) {
                group = new NotificationChannelGroup(groupId, groupName);
                group.setDescription("All payment-related notifications");
                notificationManager.createNotificationChannelGroup(group);
                Log.d(TAG, "Created notification channel group: " + groupId);
            } else {
                Log.d(TAG, "Notification channel group already exists: " + groupId);
            }

            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(channelDescription);
                channel.setGroup(groupId);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 300, 100, 300, 100, 300, 100, 600});
                channel.setBypassDnd(true);
                channel.setShowBadge(true);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build();
                channel.setSound(soundUri, audioAttributes);

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Created notification channel: " + channelId);
            } else {
                Log.d(TAG, "Notification channel already exists: " + channelId);
                boolean needsUpdate = channel.getImportance() != NotificationManager.IMPORTANCE_HIGH ||
                        channel.getLockscreenVisibility() != NotificationCompat.VISIBILITY_PUBLIC ||
                        !channel.shouldShowLights() || channel.getLightColor() != Color.GREEN ||
                        !channel.shouldVibrate() || !channel.getVibrationPattern().equals(new long[]{0, 300, 100, 300, 100, 300, 100, 600}) ||
                        !channel.canBypassDnd() || !channel.canShowBadge() ||
                        !channel.getSound().equals(soundUri) || !channel.getAudioAttributes().equals(audioAttributes);

                if (needsUpdate) {
                    channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                    channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    channel.enableLights(true);
                    channel.setLightColor(Color.GREEN);
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{0, 300, 100, 300, 100, 300, 100, 600});
                    channel.setBypassDnd(true);
                    channel.setShowBadge(true);
                    channel.setSound(soundUri, audioAttributes);
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Updated notification channel: " + channelId);
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception creating notification channel: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel: " + e.getMessage(), e);
        }
    }

    public static String getDefaultChannelId() {
        return DEFAULT_CHANNEL_ID;
    }

    public static String getDefaultGroupId() {
        return DEFAULT_GROUP_ID;
    }
}