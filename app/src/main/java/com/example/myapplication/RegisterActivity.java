package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * RegisterActivity registers new users with Firebase Authentication.
 *
 * Implements:
 * 1. Client-side validation: Name, Email format, Password length (>= 6 chars), and Password match.
 * 2. Real Firebase user creation with display name profile binding.
 * 3. Loading state indicators and human-readable failure messages.
 * 4. Passwords are NEVER written to logs or external storage.
 * 5. Complete Activity lifecycle logging.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private ProgressBar pbLoading;
    private TextView tvErrorMessage;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RegisterActivity: onCreate() - Binding registration views");
        setContentView(R.layout.activity_register);

        bindViews();
        setupListeners();
        setupBackNavigation();
    }

    private void bindViews() {
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etRegisterPhone);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        pbLoading = findViewById(R.id.pbRegisterLoading);
        tvErrorMessage = findViewById(R.id.tvRegisterErrorMessage);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());

        TextView tvToLogin = findViewById(R.id.tvRegisterToLogin);
        if (tvToLogin != null) {
            tvToLogin.setOnClickListener(v -> {
                Log.d(TAG, "RegisterActivity: Navigating back to Login");
                finish();
            });
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "RegisterActivity: Back pressed, returning to LoginActivity");
                finish();
            }
        });
    }

    private void handleRegistration() {
        hideError();

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Client validations
        if (TextUtils.isEmpty(name)) {
            showError("Full Name is required.");
            etName.requestFocus();
            return;
        }

        if (!FirebaseAuthService.isValidEmail(email)) {
            showError("Please enter a valid email address (e.g. user@ghorcycle.com).");
            etEmail.requestFocus();
            return;
        }

        if (!FirebaseAuthService.isValidPassword(password)) {
            showError("Password must be at least 6 characters long.");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match. Please re-enter.");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);
        Log.d(TAG, "RegisterActivity: Submitting registration for email=" + email + ", name=" + name);

        FirebaseAuthService.registerUser(email, password, name, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoading(false);
                Log.d(TAG, "RegisterActivity: Account created successfully for UID=" + (user != null ? user.getUid() : ""));
                Toast.makeText(RegisterActivity.this, "Account created! Welcome to GhorCycle, " + name, Toast.LENGTH_SHORT).show();

                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                mainIntent.putExtra(LoginActivity.EXTRA_USER_NAME, name);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Log.d(TAG, "RegisterActivity: Registration failed -> " + errorMessage);
                showError(errorMessage);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (pbLoading != null) {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(!isLoading);
        }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void hideError() {
        if (tvErrorMessage != null) {
            tvErrorMessage.setVisibility(View.GONE);
        }
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
    }
}
