package com.digikhata.audionotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "PaymentBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Boot receiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            try {
                Log.d(TAG, "Initializing payment notification system after boot/update");

                NotificationChannelHelper.createPaymentNotificationChannel(
                        context,
                        NotificationChannelHelper.DEFAULT_CHANNEL_ID,
                        NotificationChannelHelper.DEFAULT_CHANNEL_NAME,
                        NotificationChannelHelper.DEFAULT_CHANNEL_DESCRIPTION,
                        NotificationChannelHelper.DEFAULT_GROUP_ID,
                        NotificationChannelHelper.DEFAULT_GROUP_NAME
                );
                Log.d(TAG, "Notification channel initialized successfully");

                Intent serviceIntent = new Intent(context, PaymentMessagingService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                Log.d(TAG, "PaymentMessagingService started successfully");

            } catch (SecurityException e) {
                Log.e(TAG, "Security exception during initialization: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize payment system after boot: " + e.getMessage(), e);
            }
        }
    }
}