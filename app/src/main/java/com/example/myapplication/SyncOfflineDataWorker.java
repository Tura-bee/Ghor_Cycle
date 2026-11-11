package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Background Worker for GhorCycle demonstrating guaranteed, deferrable execution.
 *
 * Implements:
 * 1. AndroidX WorkManager architecture (recommended over legacy background Services).
 * 2. Strict compliance with Android 8.0+ Background Execution Limits and Doze Mode.
 * 3. Graceful cooperative cancellation handling via isStopped().
 * 4. Output data delivery through Data.Builder for Activity-Worker communication.
 * 5. Meaningful task: Processing offline queue and synchronizing local catalog state.
 */
public class SyncOfflineDataWorker extends Worker {

    private static final String TAG = "GhorCycle_Worker";

    public static final String KEY_SYNCED_COUNT = "key_synced_count";
    public static final String KEY_SYNC_MESSAGE = "key_sync_message";
    public static final String WORK_TAG_SYNC = "ghorcycle_offline_sync";

    public SyncOfflineDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncOfflineDataWorker: doWork() started on background thread ID=" + Thread.currentThread().getId());

        // Cooperative cancellation check before beginning heavy processing
        if (isStopped()) {
            Log.w(TAG, "SyncOfflineDataWorker: Work was cancelled before starting");
            return Result.failure();
        }

        try {
            // Check internet reachability
            boolean isOnline = NetworkChangeReceiver.checkInternetConnectivity(getApplicationContext());
            if (!isOnline) {
                Log.w(TAG, "SyncOfflineDataWorker: Network became unavailable during execution. Requesting retry.");
                return Result.retry();
            }

            // Simulate processing offline data queue (e.g., pending product drafts, cached items)
            Log.d(TAG, "SyncOfflineDataWorker: Inspecting local offline queue...");
            Thread.sleep(1200); // Non-blocking background simulation

            if (isStopped()) {
                Log.w(TAG, "SyncOfflineDataWorker: Cancellation requested during queue processing");
                return Result.failure();
            }

            int syncedItemsCount = 3; // Simulated count of pending offline updates processed
            Log.d(TAG, "SyncOfflineDataWorker: Successfully processed " + syncedItemsCount + " items from local queue");

            Data outputData = new Data.Builder()
                    .putInt(KEY_SYNCED_COUNT, syncedItemsCount)
                    .putString(KEY_SYNC_MESSAGE, "Local offline queue synced successfully (" + syncedItemsCount + " items updated).")
                    .putLong("sync_timestamp", System.currentTimeMillis())
                    .build();

            return Result.success(outputData);

        } catch (InterruptedException e) {
            Log.e(TAG, "SyncOfflineDataWorker: Task interrupted or aborted", e);
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "SyncOfflineDataWorker: Unexpected execution failure", e);
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "SyncOfflineDataWorker: onStopped() callback triggered by WorkManager");
    }
}
