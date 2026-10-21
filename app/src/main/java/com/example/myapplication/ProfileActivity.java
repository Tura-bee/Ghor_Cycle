package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ProfileActivity displays the user's community presence, cycling statistics, and account management.
 *
 * Demonstrates:
 * 1. Intent data extraction for user profile.
 * 2. Explicit Intent navigation back to LoginActivity on sign out.
 * 3. Up/Back navigation to MainActivity.
 * 4. Custom Back-press handling with OnBackPressedDispatcher.
 * 5. Complete Activity lifecycle logging.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    public static final String EXTRA_PROFILE_NAME = "com.ghorcycle.extra.PROFILE_NAME";

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProfileActivity: onCreate() - Binding profile views and action handlers");
        setContentView(R.layout.activity_profile);

        if (getIntent() != null) {
            userName = getIntent().getStringExtra(EXTRA_PROFILE_NAME);
        }
        if (TextUtils.isEmpty(userName)) {
            userName = "Arif Hossain";
        }

        TextView tvName = findViewById(R.id.tvProfileName);
        tvName.setText(userName);

        Button btnBack = findViewById(R.id.btnProfileBack);
        Button btnEdit = findViewById(R.id.btnProfileEdit);
        Button btnListings = findViewById(R.id.btnProfileListings);
        Button btnLogout = findViewById(R.id.btnProfileLogout);

        // Click listeners
        btnBack.setOnClickListener(v -> handleBackNavigation());

        btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "ProfileActivity: Edit profile requested");
            Toast.makeText(this, "Profile editing will be enabled in next stage.", Toast.LENGTH_SHORT).show();
        });

        btnListings.setOnClickListener(v -> {
            Log.d(TAG, "ProfileActivity: My listings requested");
            Toast.makeText(this, "Showing 6 active listings for " + userName, Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "ProfileActivity: Logging out user and clearing task stack");
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });

        // Register Back-press dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void handleBackNavigation() {
        Log.d(TAG, "ProfileActivity: Returning to MainActivity");
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ProfileActivity: onStart() - Activity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ProfileActivity: onResume() - Activity focused");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ProfileActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ProfileActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "ProfileActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProfileActivity: onDestroy() - Activity destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "ProfileActivity: onSaveInstanceState() - Saving user profile state");
        outState.putString("saved_username", userName);
    }
}
