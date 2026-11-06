package com.example.myapplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Pattern;

/**
 * Service managing Firebase Authentication operations for GhorCycle.
 *
 * Implements:
 * 1. Safe FirebaseApp availability detection.
 * 2. User registration with display name profile updates.
 * 3. User sign in and sign out.
 * 4. Pure Java input validation (standard RFC email format & min 6 char password).
 * 5. Human-readable error message translation without logging credentials.
 */
public class FirebaseAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    );

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    /**
     * Checks if Firebase is initialized and available in the current runtime.
     */
    public static boolean isFirebaseConfigured() {
        try {
            return FirebaseApp.getApps(FirebaseAuth.getInstance().getApp().getApplicationContext()).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates email format according to standard RFC-compliant regex.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates password strength (minimum 6 characters required by Firebase Authentication).
     */
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.trim().length() >= 6;
    }

    /**
     * Returns currently authenticated FirebaseUser, or null if logged out / unconfigured.
     */
    public static FirebaseUser getCurrentUser() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Registers a new user with email, password, and display name.
     */
    public static void registerUser(String email, String password, final String displayName, final AuthCallback callback) {
        if (!isValidEmail(email)) {
            callback.onFailure("Please provide a valid email address (e.g. user@example.com).");
            return;
        }

        if (!isValidPassword(password)) {
            callback.onFailure("Password must be at least 6 characters long.");
            return;
        }

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null && displayName != null && !displayName.trim().isEmpty()) {
                            // Update display name profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName.trim())
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> callback.onSuccess(user));
                        } else {
                            callback.onSuccess(user);
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure(translateAuthException(e)));
        } catch (IllegalStateException e) {
            callback.onFailure("Firebase project is not yet configured. Please ensure google-services.json is added to the app/ directory.");
        }
    }

    /**
     * Signs in an existing user with email and password.
     */
    public static void loginUser(String email, String password, final AuthCallback callback) {
        if (!isValidEmail(email)) {
            callback.onFailure("Please enter a valid email address.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            callback.onFailure("Please enter your password.");
            return;
        }

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                    .addOnFailureListener(e -> callback.onFailure(translateAuthException(e)));
        } catch (IllegalStateException e) {
            callback.onFailure("Firebase project is not yet configured. Please ensure google-services.json is added to the app/ directory.");
        }
    }

    /**
     * Signs out the currently authenticated user.
     */
    public static void signOut() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {
        }
    }

    /**
     * Translates Firebase auth exceptions into clean, readable, actionable feedback for users.
     */
    public static String translateAuthException(Exception e) {
        if (e == null) return "An unknown authentication error occurred.";

        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Please use at least 6 characters with mixed letters and numbers.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid email address or incorrect password. Please check your credentials.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return "An account with this email already exists. Please sign in instead.";
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            return "No registered account found with this email. Please check your email or create an account.";
        } else if (e.getClass().getSimpleName().contains("Network")) {
            return "Network connection error. Please check your internet connection and retry.";
        }

        String msg = e.getMessage();
        if (msg != null && !msg.trim().isEmpty()) {
            return msg;
        }
        return "Authentication request failed. Please check your network and try again.";
    }
}
