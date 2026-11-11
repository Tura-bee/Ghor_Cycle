package com.example.ghor_cycle;

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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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
    private Button btnGoogle;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RegisterActivity: onCreate() - Binding registration views");
        setContentView(R.layout.activity_register);
        FirebaseAuthService.init(this);

        bindViews();
        setupGoogleSignIn();
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
        btnGoogle = findViewById(R.id.btnRegisterGoogle);
    }

    private void setupGoogleSignIn() {
        String webClientId;
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            webClientId = (resId != 0) ? getString(resId) : getString(R.string.default_web_client_id);
        } catch (Exception e) {
            webClientId = getString(R.string.default_web_client_id);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        setLoading(false);
                        Log.w(TAG, "RegisterActivity: Google sign-in cancelled or returned resultCode=" + result.getResultCode());
                    }
                }
        );
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "RegisterActivity: Google sign-in successful, authenticating with Firebase");
                FirebaseAuthService.signInWithGoogle(account.getIdToken(), new FirebaseAuthService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "Welcome to GhorCycle, " + (account.getDisplayName() != null ? account.getDisplayName() : "") + "!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity(user);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        setLoading(false);
                        showError(errorMessage);
                    }
                });
            } else {
                setLoading(false);
                showError("Unable to retrieve Google ID token.");
            }
        } catch (ApiException e) {
            setLoading(false);
            Log.e(TAG, "RegisterActivity: Google sign in failed code=" + e.getStatusCode(), e);
            showError("Google sign-in failed (Error " + e.getStatusCode() + "). Please ensure Google Sign-In is enabled in Firebase.");
        }
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                hideError();
                setLoading(true);
                Log.d(TAG, "RegisterActivity: Starting Google Sign-In intent");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }

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

    private void navigateToMainActivity(FirebaseUser user) {
        String displayName = FirebaseAuthService.getCurrentUserName();

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.putExtra(LoginActivity.EXTRA_USER_NAME, displayName);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (pbLoading != null) {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(!isLoading);
        }
        if (btnGoogle != null) {
            btnGoogle.setEnabled(!isLoading);
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
