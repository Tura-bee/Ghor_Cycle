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
 * RegisterActivity enables new users to join the GhorCycle community.
 *
 * Demonstrates:
 * 1. Comprehensive input validation for registration credentials.
 * 2. Explicit Intent navigation to MainActivity upon successful registration.
 * 3. Smooth return navigation to LoginActivity.
 * 4. Custom Back-press handling using AndroidX OnBackPressedDispatcher.
 * 5. Lifecycle callback demonstrations and logging.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RegisterActivity: onCreate() - Setting layout and configuring form controls");
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etRegisterPhone);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegisterSubmit);
        TextView tvToLogin = findViewById(R.id.tvRegisterToLogin);

        // Register Button Listener
        btnRegister.setOnClickListener(v -> handleRegistration());

        // Back to Login Listener
        tvToLogin.setOnClickListener(v -> {
            Log.d(TAG, "RegisterActivity: User navigated back to Login");
            finish(); // Return to LoginActivity
        });

        // System back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "RegisterActivity: Back pressed, returning to LoginActivity");
                finish();
            }
        });
    }

    private void handleRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Log.d(TAG, "RegisterActivity: Registration successful for user=" + name + ", email=" + email);
        Toast.makeText(this, "Account created! Welcome, " + name, Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity via explicit Intent
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.putExtra(LoginActivity.EXTRA_USER_NAME, name);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "RegisterActivity: onStart() - Activity becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RegisterActivity: onResume() - Activity focused and interactive");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "RegisterActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "RegisterActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "RegisterActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RegisterActivity: onDestroy() - Activity destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "RegisterActivity: onSaveInstanceState() - Saving form fields across rotation");
        if (etName != null) outState.putString("saved_name", etName.getText().toString());
        if (etEmail != null) outState.putString("saved_email", etEmail.getText().toString());
        if (etPhone != null) outState.putString("saved_phone", etPhone.getText().toString());
    }
}
