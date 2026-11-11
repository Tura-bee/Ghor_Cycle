package com.example.ghor_cycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * SplashActivity serves as the launcher entry point for the GhorCycle application.
 *
 * Implements:
 * 1. Branded entrance with app name and tagline.
 * 2. Persistent Firebase Authentication check routing:
 *    - Authenticated users -> MainActivity
 *    - Unauthenticated users -> LoginActivity
 * 3. Immediate "Get Started" manual skip and automatic 2.2s transition.
 * 4. Safe lifecycle timer cleanup on destroy.
 * 5. Comprehensive Activity lifecycle logging.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    private static final int SPLASH_DELAY_MS = 2200;

    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Runnable transitionRunnable;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SplashActivity: onCreate() - Initializing view and auth state check");
        setContentView(R.layout.activity_splash);
        FirebaseAuthService.init(this);

        Button btnGetStarted = findViewById(R.id.btnSplashGetStarted);

        // Immediate navigation when user clicks Get Started
        btnGetStarted.setOnClickListener(v -> {
            Log.d(TAG, "SplashActivity: 'Get Started' clicked by user");
            routeBasedOnAuthState();
        });

        // Automatic delayed navigation
        transitionRunnable = () -> {
            Log.d(TAG, "SplashActivity: Splash delay elapsed, checking auth state automatically");
            routeBasedOnAuthState();
        };
        splashHandler.postDelayed(transitionRunnable, SPLASH_DELAY_MS);
    }

    private void routeBasedOnAuthState() {
        if (hasNavigated) return;
        hasNavigated = true;

        if (transitionRunnable != null) {
            splashHandler.removeCallbacks(transitionRunnable);
        }

        // Check persistent Firebase Authentication state or local session
        boolean isLogged = FirebaseAuthService.isUserLoggedIn();
        Intent intent;

        if (isLogged) {
            String displayName = FirebaseAuthService.getCurrentUserName();
            String uid = FirebaseAuthService.getCurrentUserId();

            Log.d(TAG, "SplashActivity: Persistent session found for UID=" + uid + ", routing to MainActivity");
            intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra(LoginActivity.EXTRA_USER_NAME, displayName);
        } else {
            Log.d(TAG, "SplashActivity: No authenticated session found, routing to LoginActivity");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Remove Splash from the back stack
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "SplashActivity: onStart() - Activity visible to user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SplashActivity: onResume() - Activity ready for user interaction");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "SplashActivity: onPause() - Activity losing focus");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "SplashActivity: onStop() - Activity no longer visible");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "SplashActivity: onRestart() - Activity restarting after being stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity: onDestroy() - Cleaning up pending splash timers");
        if (transitionRunnable != null) {
            splashHandler.removeCallbacks(transitionRunnable);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "SplashActivity: onSaveInstanceState() - Saving state");
        outState.putBoolean("hasNavigated", hasNavigated);
    }
}
