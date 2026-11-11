package com.example.ghor_cycle;

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
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity displays the user's community presence, cycling statistics,
 * and account authentication management.
 *
 * Implements:
 * 1. Live Firebase Authentication user profile rendering.
 * 2. Dynamic state handling for authenticated vs guest users.
 * 3. Real Firebase sign out with task affinity clearing.
 * 4. Back navigation to MainActivity.
 * 5. Complete Activity lifecycle logging.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    public static final String EXTRA_PROFILE_NAME = "com.ghorcycle.extra.PROFILE_NAME";

    private String userName;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProfileActivity: onCreate() - Binding profile views and checking auth status");
        setContentView(R.layout.activity_profile);
        FirebaseAuthService.init(this);

        currentUser = FirebaseAuthService.getCurrentUser();

        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvLocation = findViewById(R.id.tvProfileLocation);

        Button btnBack = findViewById(R.id.btnProfileBack);
        Button btnEdit = findViewById(R.id.btnProfileEdit);
        Button btnListings = findViewById(R.id.btnProfileListings);
        Button btnEcoImpact = findViewById(R.id.btnProfileEcoImpact);
        Button btnRecyclingDirectory = findViewById(R.id.btnProfileRecyclingDirectory);
        Button btnLogout = findViewById(R.id.btnProfileLogout);

        if (FirebaseAuthService.isUserLoggedIn()) {
            String displayName = FirebaseAuthService.getCurrentUserName();
            String email = FirebaseAuthService.getCurrentUserEmail();
            String uid = FirebaseAuthService.getCurrentUserId();

            userName = displayName;
            tvName.setText(displayName);
            tvEmail.setText(email != null ? email : "Local Account");
            tvLocation.setText("UID: " + (uid != null ? uid.substring(0, Math.min(12, uid.length())) : "local") + "…");

            btnLogout.setText("Sign Out");
            btnLogout.setBackgroundColor(ContextCompat.getColor(this, R.color.status_danger));
        } else {
            userName = "Guest Neighbor";
            tvName.setText("Guest Neighbor");
            tvEmail.setText("Not signed in");
            tvLocation.setText("Sign in to list items and track community recycling");

            btnLogout.setText("Sign In / Register");
            btnLogout.setBackgroundColor(ContextCompat.getColor(this, R.color.eco_green_primary));
        }

        // Click listeners
        btnBack.setOnClickListener(v -> handleBackNavigation());

        btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "ProfileActivity: Edit profile requested");
            Toast.makeText(this, "Profile editing will be enabled in next stage.", Toast.LENGTH_SHORT).show();
        });

        btnListings.setOnClickListener(v -> {
            Log.d(TAG, "ProfileActivity: My listings requested");
            Toast.makeText(this, "Showing active listings for " + userName, Toast.LENGTH_SHORT).show();
        });

        if (btnEcoImpact != null) {
            btnEcoImpact.setOnClickListener(v -> {
                Log.d(TAG, "ProfileActivity: Launching EcoImpactActivity");
                Intent intent = new Intent(ProfileActivity.this, EcoImpactActivity.class);
                startActivity(intent);
            });
        }

        if (btnRecyclingDirectory != null) {
            btnRecyclingDirectory.setOnClickListener(v -> {
                Log.d(TAG, "ProfileActivity: Launching RecyclingDirectoryActivity");
                Intent intent = new Intent(ProfileActivity.this, RecyclingDirectoryActivity.class);
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> {
            if (currentUser != null) {
                Log.d(TAG, "ProfileActivity: Signing out Firebase user UID=" + currentUser.getUid());
                FirebaseAuthService.signOut();
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "ProfileActivity: Navigating guest to LoginActivity");
            }

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
