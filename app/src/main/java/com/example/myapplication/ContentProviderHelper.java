package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.InputStream;

/**
 * Educational and architectural helper demonstrating safe Content Provider interaction.
 *
 * Concepts Demonstrated:
 * 1. Purpose of Content Providers:
 *    Content Providers act as a secure, structured abstraction layer for data access across processes.
 *    They encapsulate data access behind content:// URIs and standard CRUD contracts.
 *
 * 2. Why an app should NOT create an unnecessary custom ContentProvider:
 *    If an application does not share proprietary data with other third-party apps, exposing a custom
 *    ContentProvider introduces unnecessary security attack surfaces (SQL injection, path traversal)
 *    and increases manifest complexity. Instead, consuming existing system Content Providers
 *    (e.g., MediaStore, PhotoPicker, DocumentsProvider) is the recommended best practice.
 *
 * 3. Safe URI Access & Missing File Handling:
 *    Safely probes ContentResolver with error boundaries for missing, deleted, or unreadable streams.
 *
 * 4. Principle of Least Privilege:
 *    Zero broad storage permissions (such as READ_EXTERNAL_STORAGE or MANAGE_EXTERNAL_STORAGE) are requested.
 *    The Android Photo Picker grants granular, scoped read permission only for the user-selected URI.
 */
public class ContentProviderHelper {

    private static final String TAG = "GhorCycle_ContentProvider";

    public static class FileMetadata {
        public final String displayName;
        public final long sizeBytes;
        public final boolean isAccessible;

        public FileMetadata(String displayName, long sizeBytes, boolean isAccessible) {
            this.displayName = displayName;
            this.sizeBytes = sizeBytes;
            this.isAccessible = isAccessible;
        }

        public String getFormattedSize() {
            if (sizeBytes <= 0) return "Unknown size";
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Resolves metadata from a content:// URI using the Android ContentResolver.
     */
    public static FileMetadata resolveUriMetadata(Context context, Uri uri) {
        if (context == null || uri == null) {
            return new FileMetadata("Unknown", 0, false);
        }

        String displayName = "Selected_Image.jpg";
        long size = 0;
        boolean accessible = false;

        ContentResolver resolver = context.getContentResolver();

        // 1. Query metadata cursor from ContentProvider
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex);
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "ContentResolver metadata query failed for URI: " + uri, e);
        }

        // 2. Validate input stream accessibility (safely handle missing/deleted file)
        try (InputStream is = resolver.openInputStream(uri)) {
            accessible = (is != null);
        } catch (Exception e) {
            Log.w(TAG, "ContentResolver stream unreadable or missing for URI: " + uri, e);
            accessible = false;
        }

        return new FileMetadata(displayName, size, accessible);
    }
}
