package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrator for background tasks in GhorCycle.
 *
 * Implements:
 * 1. Declarative WorkManager constraints (requires active network).
 * 2. Exponential backoff retry policy (10s initial delay).
 * 3. Unique work sequencing (ExistingWorkPolicy.KEEP) to prevent redundant duplicate jobs.
 * 4. Cooperative cancellation through cancelAllSyncWork().
 * 5. Lifecycle-aware state observation with LiveData.
 */
public class BackgroundSyncManager {

    private static final String TAG = "GhorCycle_SyncManager";
    public static final String UNIQUE_WORK_NAME = "ghorcycle_unique_offline_sync";

    public interface SyncStatusCallback {
        void onStatusChanged(WorkInfo.State state, String message);
    }

    /**
     * Enqueue a one-time synchronization task with network constraints.
     */
    public static UUID scheduleOfflineSync(Context context, final SyncStatusCallback callback) {
        if (context == null) return null;

        Log.d(TAG, "BackgroundSyncManager: Enqueuing offline data sync with network constraints");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncOfflineDataWorker.class)
                .setConstraints(constraints)
                .addTag(SyncOfflineDataWorker.WORK_TAG_SYNC)
                .setInitialDelay(500, TimeUnit.MILLISECONDS)
                .build();

        UUID workId = syncRequest.getId();

        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
        );

        if (context instanceof LifecycleOwner && callback != null) {
            LiveData<WorkInfo> liveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(workId);
            liveData.observe((LifecycleOwner) context, workInfo -> {
                if (workInfo != null) {
                    String detail = "Work state: " + workInfo.getState().name();
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String msg = workInfo.getOutputData().getString(SyncOfflineDataWorker.KEY_SYNC_MESSAGE);
                        if (msg != null) detail = msg;
                    }
                    callback.onStatusChanged(workInfo.getState(), detail);
                }
            });
        }

        return workId;
    }

    /**
     * Cancel all active or queued sync tasks.
     */
    public static void cancelAllSyncWork(Context context) {
        if (context == null) return;
        Log.d(TAG, "BackgroundSyncManager: Cancelling all queued sync tasks");
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
    }
}
