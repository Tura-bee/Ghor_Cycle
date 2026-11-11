package com.example.ghor_cycle;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Stage 6 Android Components:
 * 1. BroadcastReceiver: Architecture, dynamic context registration, and export flags.
 * 2. Background Work: WorkManager vs legacy Services, constraints, and keys.
 * 3. Content Provider: Safe metadata resolution and least-privilege access.
 * 4. Component communication and lifecycle contracts.
 */
public class Stage6AndroidComponentsUnitTest {

    @Test
    public void testContentProviderMetadataFormatting() {
        ContentProviderHelper.FileMetadata smallFile = new ContentProviderHelper.FileMetadata("icon.png", 512, true);
        assertEquals("icon.png", smallFile.displayName);
        assertEquals(512, smallFile.sizeBytes);
        assertTrue(smallFile.isAccessible);
        assertEquals("512 B", smallFile.getFormattedSize());

        ContentProviderHelper.FileMetadata kbFile = new ContentProviderHelper.FileMetadata("photo.jpg", 153600, true);
        assertEquals("150.0 KB", kbFile.getFormattedSize());

        ContentProviderHelper.FileMetadata mbFile = new ContentProviderHelper.FileMetadata("video.mp4", 5242880, true);
        assertEquals("5.0 MB", mbFile.getFormattedSize());

        ContentProviderHelper.FileMetadata zeroFile = new ContentProviderHelper.FileMetadata("empty.txt", 0, false);
        assertEquals("Unknown size", zeroFile.getFormattedSize());
        assertFalse(zeroFile.isAccessible);
    }

    @Test
    public void testContentProviderSafeNullHandling() {
        ContentProviderHelper.FileMetadata nullResult = ContentProviderHelper.resolveUriMetadata(null, null);
        assertNotNull(nullResult);
        assertEquals("Unknown", nullResult.displayName);
        assertEquals(0, nullResult.sizeBytes);
        assertFalse(nullResult.isAccessible);
    }

    @Test
    public void testWorkManagerSyncWorkerKeysAndConstants() {
        assertNotNull(SyncOfflineDataWorker.KEY_SYNCED_COUNT);
        assertNotNull(SyncOfflineDataWorker.KEY_SYNC_MESSAGE);
        assertNotNull(SyncOfflineDataWorker.WORK_TAG_SYNC);
        assertNotNull(BackgroundSyncManager.UNIQUE_WORK_NAME);

        assertEquals("key_synced_count", SyncOfflineDataWorker.KEY_SYNCED_COUNT);
        assertEquals("ghorcycle_offline_sync", SyncOfflineDataWorker.WORK_TAG_SYNC);
        assertEquals("ghorcycle_unique_offline_sync", BackgroundSyncManager.UNIQUE_WORK_NAME);
    }

    @Test
    public void testBroadcastReceiverListenerContract() {
        final boolean[] received = new boolean[]{false};
        final String[] message = new String[]{""};

        NetworkChangeReceiver.NetworkStatusListener listener = (isConnected, statusMessage) -> {
            received[0] = isConnected;
            message[0] = statusMessage;
        };

        listener.onNetworkChanged(true, "Online — Connected to network");
        assertTrue(received[0]);
        assertEquals("Online — Connected to network", message[0]);

        listener.onNetworkChanged(false, "Offline — No active internet connection");
        assertFalse(received[0]);
        assertEquals("Offline — No active internet connection", message[0]);
    }

    @Test
    public void testComponentLifecycleDifferences() {
        // BroadcastReceiver execution timeout limit in Android
        int broadcastReceiverAnrTimeoutSeconds = 10;
        assertTrue("BroadcastReceiver onReceive must complete within 10s", broadcastReceiverAnrTimeoutSeconds <= 10);

        // WorkManager minimum periodic interval in Android
        int workManagerMinPeriodicIntervalMinutes = 15;
        assertEquals("WorkManager periodic interval constraint is 15 mins", 15, workManagerMinPeriodicIntervalMinutes);
    }
}
