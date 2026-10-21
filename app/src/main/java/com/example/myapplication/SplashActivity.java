package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity serves as the launcher entry point for the GhorCycle application.
 *
 * Demonstrates:
 * 1. Branded entrance with app name and tagline.
 * 2. Explicit Intent navigation to LoginActivity.
 * 3. Safe Handler postDelayed scheduling with lifecycle cleanup on destroy.
 * 4. Comprehensive Activity lifecycle logging.
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
        Log.d(TAG, "SplashActivity: onCreate() - Initializing view and timer");
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnSplashGetStarted);

        // Immediate navigation when user clicks Get Started
        btnGetStarted.setOnClickListener(v -> {
            Log.d(TAG, "SplashActivity: 'Get Started' clicked by user");
            navigateToLogin();
        });

        // Automatic delayed navigation
        transitionRunnable = () -> {
            Log.d(TAG, "SplashActivity: Splash delay elapsed, navigating automatically");
            navigateToLogin();
        };
        splashHandler.postDelayed(transitionRunnable, SPLASH_DELAY_MS);
    }

    private void navigateToLogin() {
        if (hasNavigated) return;
        hasNavigated = true;

        if (transitionRunnable != null) {
            splashHandler.removeCallbacks(transitionRunnable);
        }

        // Explicit Intent navigation to LoginActivity
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
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
