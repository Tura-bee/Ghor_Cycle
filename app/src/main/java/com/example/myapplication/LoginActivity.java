package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity provides user authentication entry for GhorCycle.
 *
 * Demonstrates:
 * 1. Explicit Intent navigation to MainActivity upon sign in or guest browsing.
 * 2. Explicit Intent navigation to RegisterActivity.
 * 3. Input validation for credentials with user feedback.
 * 4. Custom Back-press handling using AndroidX OnBackPressedDispatcher.
 * 5. Complete Activity lifecycle logging.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity: onCreate() - Setting layout and registering listeners");
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLoginSubmit);
        Button btnGuest = findViewById(R.id.btnLoginGuest);
        TextView tvToRegister = findViewById(R.id.tvLoginToRegister);

        // Sign In Button Listener
        btnLogin.setOnClickListener(v -> handleSignIn());

        // Guest Access Button Listener
        btnGuest.setOnClickListener(v -> {
            Log.d(TAG, "LoginActivity: Guest login selected");
            Intent guestIntent = new Intent(LoginActivity.this, MainActivity.class);
            guestIntent.putExtra(EXTRA_USER_NAME, "Guest Neighbor");
            startActivity(guestIntent);
            finish();
        });

        // Navigate to RegisterActivity
        tvToRegister.setOnClickListener(v -> {
            Log.d(TAG, "LoginActivity: Navigating to RegisterActivity");
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });

        // Back button callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "LoginActivity: System back pressed - exiting application");
                finishAffinity(); // Cleanly exit the app
            }
        });
    }

    private void handleSignIn() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email or phone");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        Log.d(TAG, "LoginActivity: Sign in successful for " + email);
        Toast.makeText(this, "Welcome back, " + email + "!", Toast.LENGTH_SHORT).show();

        // Explicit Intent to MainActivity
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.putExtra(EXTRA_USER_NAME, email);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "LoginActivity: onStart() - Activity becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LoginActivity: onResume() - Activity ready for user credentials");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LoginActivity: onPause() - Activity entering paused state");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LoginActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "LoginActivity: onRestart() - Activity returning from stopped state");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity: onDestroy() - Activity resources destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "LoginActivity: onSaveInstanceState() - Preserving transient UI state");
        if (etEmail != null) {
            outState.putString("saved_email", etEmail.getText().toString());
        }
    }
}
