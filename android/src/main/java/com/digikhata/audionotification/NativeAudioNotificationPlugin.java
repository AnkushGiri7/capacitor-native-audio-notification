package com.digikhata.audionotification;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import android.util.Log;

@CapacitorPlugin(name = "NativeAudioNotification")
public class NativeAudioNotificationPlugin extends Plugin {
    private static final String TAG = "NativeAudioNotificationPlugin";
    private NativeAudioNotification implementation;

    @Override
    public void load() {
        try {
            implementation = new NativeAudioNotification(getContext());
        } catch (Exception e) {
            Log.e(TAG, "Error loading plugin: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void configure(PluginCall call) {
        try {
            // Future config options can be added here (e.g., TTS voice settings)
            call.resolve();
        } catch (Exception e) {
            Log.e(TAG, "Error in configure: " + e.getMessage(), e);
            call.reject("Configuration failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void testNotification(PluginCall call) {
        try {
            String amountStr = call.getString("amount");
            double amount = Double.parseDouble(amountStr != null ? amountStr : "0");
            String currency = call.getString("currency", "Rs.");
            String customerName = call.getString("customerName");
            String transactionId = call.getString("transactionId", "");
            long timestamp = call.getLong("timestamp", System.currentTimeMillis());

            if (implementation != null) {
                implementation.playPaymentNotification(String.valueOf(amount), currency, customerName, transactionId, timestamp);
                call.resolve();
            } else {
                call.reject("Implementation not initialized");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format in testNotification: " + e.getMessage(), e);
            call.reject("Invalid data format: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error in testNotification: " + e.getMessage(), e);
            call.reject("Test notification failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setMerchantInfo(PluginCall call) {
        try {
            String businessName = call.getString("businessName");
            if (implementation != null) {
                implementation.setMerchantInfo(businessName);
                JSObject ret = new JSObject();
                ret.put("businessName", implementation.getMerchantBusiness()); // Return current value
                call.resolve(ret);
            } else {
                call.reject("Implementation not initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setMerchantInfo: " + e.getMessage(), e);
            call.reject("Set merchant info failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void toggleNotifications(PluginCall call) {
        try {
            boolean enabled = call.getBoolean("enabled", true);
            // Placeholder: Add logic to start/stop foreground service or FCM listener if needed
            JSObject ret = new JSObject();
            ret.put("enabled", enabled);
            call.resolve(ret);
        } catch (Exception e) {
            Log.e(TAG, "Error in toggleNotifications: " + e.getMessage(), e);
            call.reject("Toggle notifications failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void playTts(PluginCall call) {
        try {
            String text = call.getString("text", "Test audio message");
            if (implementation != null) {
                implementation.playTtsOnly(text);
                call.resolve();
            } else {
                call.reject("Implementation not initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in playTts: " + e.getMessage(), e);
            call.reject("Play TTS failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void stopTts(PluginCall call) {
        try {
            if (implementation != null) {
                implementation.stopTts();
                call.resolve();
            } else {
                call.reject("Implementation not initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in stopTts: " + e.getMessage(), e);
            call.reject("Stop TTS failed: " + e.getMessage());
        }
    }

    @Override
    protected void handleOnDestroy() {
        try {
            if (implementation != null) {
                implementation.cleanup();
                implementation = null; // Nullify to prevent reuse
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in handleOnDestroy: " + e.getMessage(), e);
        }
        super.handleOnDestroy();
    }
}