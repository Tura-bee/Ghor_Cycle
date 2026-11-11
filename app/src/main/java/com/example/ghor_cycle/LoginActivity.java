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
 * LoginActivity provides real Firebase Authentication entry for GhorCycle.
 *
 * Implements:
 * 1. Email format and password validation before submission.
 * 2. Real Firebase Authentication with loading indicators and human-readable error feedback.
 * 3. Guest browsing option for viewing marketplace items without credentials.
 * 4. Safe credential handling (passwords are NEVER logged).
 * 5. Complete Activity lifecycle logging.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private EditText etEmail;
    private EditText etPassword;
    private ProgressBar pbLoading;
    private TextView tvErrorMessage;
    private Button btnLogin;
    private Button btnGoogle;
    private Button btnGuest;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity: onCreate() - Binding views and checking auth status");
        setContentView(R.layout.activity_login);
        FirebaseAuthService.init(this);

        // Check if already authenticated via Firebase or local session
        if (FirebaseAuthService.isUserLoggedIn()) {
            Log.d(TAG, "LoginActivity: User session already active, routing to MainActivity");
            navigateToMainActivity(FirebaseAuthService.getCurrentUser());
            return;
        }

        bindViews();
        setupGoogleSignIn();
        setupListeners();
        setupBackNavigation();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        pbLoading = findViewById(R.id.pbLoginLoading);
        tvErrorMessage = findViewById(R.id.tvLoginErrorMessage);
        btnLogin = findViewById(R.id.btnLoginSubmit);
        btnGoogle = findViewById(R.id.btnLoginGoogle);
        btnGuest = findViewById(R.id.btnLoginGuest);
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
                        Log.w(TAG, "LoginActivity: Google sign-in cancelled or returned resultCode=" + result.getResultCode());
                    }
                }
        );
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "LoginActivity: Google sign-in successful, authenticating with Firebase");
                FirebaseAuthService.signInWithGoogle(account.getIdToken(), new FirebaseAuthService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Welcome to GhorCycle, " + (account.getDisplayName() != null ? account.getDisplayName() : "") + "!", Toast.LENGTH_SHORT).show();
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
            Log.e(TAG, "LoginActivity: Google sign in failed code=" + e.getStatusCode(), e);
            showError("Google sign-in failed (Error " + e.getStatusCode() + "). Please ensure Google Sign-In is enabled in Firebase.");
        }
    }

    private void setupListeners() {
        // Sign In Button Listener
        btnLogin.setOnClickListener(v -> handleSignIn());

        // Google Sign In Button Listener
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                hideError();
                setLoading(true);
                Log.d(TAG, "LoginActivity: Starting Google Sign-In intent");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }

        // Guest Access Button Listener
        btnGuest.setOnClickListener(v -> {
            Log.d(TAG, "LoginActivity: Guest browsing mode selected");
            Intent guestIntent = new Intent(LoginActivity.this, MainActivity.class);
            guestIntent.putExtra(EXTRA_USER_NAME, "Guest Neighbor");
            startActivity(guestIntent);
            finish();
        });

        // Navigate to RegisterActivity
        TextView tvToRegister = findViewById(R.id.tvLoginToRegister);
        if (tvToRegister != null) {
            tvToRegister.setOnClickListener(v -> {
                Log.d(TAG, "LoginActivity: Navigating to RegisterActivity");
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            });
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "LoginActivity: System back pressed - exiting application");
                finishAffinity();
            }
        });
    }

    private void handleSignIn() {
        hideError();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Client-side validations
        if (!FirebaseAuthService.isValidEmail(email)) {
            showError("Please enter a valid email address (e.g. user@ghorcycle.com)");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);
        Log.d(TAG, "LoginActivity: Submitting login request for user=" + email);

        FirebaseAuthService.loginUser(email, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoading(false);
                Log.d(TAG, "LoginActivity: Firebase authentication successful for UID=" + (user != null ? user.getUid() : ""));
                Toast.makeText(LoginActivity.this, "Welcome back to GhorCycle!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Log.d(TAG, "LoginActivity: Authentication failed -> " + errorMessage);
                showError(errorMessage);
            }
        });
    }

    private void navigateToMainActivity(FirebaseUser user) {
        String displayName = FirebaseAuthService.getCurrentUserName();

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.putExtra(EXTRA_USER_NAME, displayName);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (pbLoading != null) {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
        if (btnGoogle != null) {
            btnGoogle.setEnabled(!isLoading);
        }
        if (btnGuest != null) {
            btnGuest.setEnabled(!isLoading);
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
        Log.d(TAG, "LoginActivity: onStart() - Activity becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LoginActivity: onResume() - Activity ready for user interaction");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LoginActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LoginActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "LoginActivity: onRestart() - Activity restarting");
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
