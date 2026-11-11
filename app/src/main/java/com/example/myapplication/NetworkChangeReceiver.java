package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * BroadcastReceiver demonstrating system event observation in compliance with Android lifecycle limits.
 *
 * Implements:
 * 1. Dynamic context registration (respects Android 7.0+ restrictions on implicit CONNECTIVITY_ACTION).
 * 2. Proper export flag handling (uses ContextCompat.RECEIVER_NOT_EXPORTED on Android 13+ / API 33).
 * 3. Two-tier verification: Broadcast trigger + active NetworkCapabilities inspection.
 *    (Crucial: Does NOT assume that receiving a connectivity intent guarantees internet reachability).
 * 4. Safe registration and unregistration lifecycle hooks to prevent Activity context leaks.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "GhorCycle_Receiver";

    public interface NetworkStatusListener {
        void onNetworkChanged(boolean isConnected, String statusMessage);
    }

    private final NetworkStatusListener listener;

    public NetworkChangeReceiver(NetworkStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;

        String action = intent != null ? intent.getAction() : "";
        Log.d(TAG, "NetworkChangeReceiver: onReceive() triggered with action=" + action);

        boolean connected = checkInternetConnectivity(context);
        String message = connected ? "Online — Connected to network" : "Offline — No active internet connection";

        if (listener != null) {
            listener.onNetworkChanged(connected, message);
        }
    }

    /**
     * Checks if the device has an active network with confirmed Internet capability.
     * Respects Android M (API 23)+ NetworkCapabilities APIs while avoiding deprecated NetworkInfo methods.
     */
    public static boolean checkInternetConnectivity(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) return false;

                NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
                if (capabilities == null) return false;

                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                @SuppressWarnings("deprecation")
                android.net.NetworkInfo activeInfo = cm.getActiveNetworkInfo();
                return activeInfo != null && activeInfo.isConnected();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking connectivity", e);
            return false;
        }
    }

    /**
     * Helper to safely register this receiver with correct export flags.
     * Uses ContextCompat.RECEIVER_NOT_EXPORTED for internal security on Android 13 (API 33)+.
     */
    public static NetworkChangeReceiver register(Context context, NetworkStatusListener listener) {
        if (context == null) return null;

        NetworkChangeReceiver receiver = new NetworkChangeReceiver(listener);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter);
        }

        Log.d(TAG, "NetworkChangeReceiver: Dynamically registered (RECEIVER_NOT_EXPORTED on API 33+)");
        return receiver;
    }

    /**
     * Helper to safely unregister this receiver.
     */
    public static void unregister(Context context, NetworkChangeReceiver receiver) {
        if (context == null || receiver == null) return;
        try {
            context.unregisterReceiver(receiver);
            Log.d(TAG, "NetworkChangeReceiver: Successfully unregistered");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver was already unregistered: " + e.getMessage());
        }
    }
}
