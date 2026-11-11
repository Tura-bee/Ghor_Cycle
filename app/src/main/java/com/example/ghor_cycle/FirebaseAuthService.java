package com.example.ghor_cycle;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Pattern;

/**
 * Service managing Firebase Authentication operations for GhorCycle with automatic local session fallback.
 *
 * Implements:
 * 1. Safe FirebaseApp availability detection.
 * 2. User registration with display name profile updates.
 * 3. User sign in and sign out.
 * 4. Automatic local device session fallback when google-services.json is pending.
 * 5. Pure Java input validation (standard RFC email format & min 6 char password).
 * 6. Human-readable error message translation without logging credentials.
 */
public class FirebaseAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    );

    private static final String PREF_NAME = "ghorcycle_auth_prefs";
    private static final String KEY_UID = "auth_uid";
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_NAME = "auth_name";

    private static Context sContext = null;
    private static String sLocalUid = null;
    private static String sLocalEmail = null;
    private static String sLocalName = null;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    /**
     * Initializes the local authentication persistence layer with application context.
     */
    public static void init(Context context) {
        if (context == null) return;
        sContext = context.getApplicationContext();
        try {
            SharedPreferences sp = sContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sLocalUid = sp.getString(KEY_UID, null);
            sLocalEmail = sp.getString(KEY_EMAIL, null);
            sLocalName = sp.getString(KEY_NAME, null);
        } catch (Exception ignored) {
        }
    }

    public static void setLocalSession(String uid, String email, String name) {
        sLocalUid = uid;
        sLocalEmail = email;
        sLocalName = name;
        if (sContext != null) {
            try {
                SharedPreferences sp = sContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit()
                        .putString(KEY_UID, uid)
                        .putString(KEY_EMAIL, email)
                        .putString(KEY_NAME, name)
                        .apply();
            } catch (Exception ignored) {
            }
        }
    }

    public static void clearLocalSession() {
        sLocalUid = null;
        sLocalEmail = null;
        sLocalName = null;
        if (sContext != null) {
            try {
                SharedPreferences sp = sContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().clear().apply();
            } catch (Exception ignored) {
            }
        }
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
     * Returns true if user is logged in either through Firebase or local session.
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null || (sLocalUid != null && !sLocalUid.isEmpty());
    }

    /**
     * Returns current user UID (from Firebase or local session).
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) return user.getUid();
        return sLocalUid;
    }

    /**
     * Returns current user email (from Firebase or local session).
     */
    public static String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        if (user != null) return user.getEmail();
        return sLocalEmail;
    }

    /**
     * Returns current user display name (from Firebase or local session).
     */
    public static String getCurrentUserName() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                return user.getDisplayName();
            }
            if (user.getEmail() != null) return user.getEmail().split("@")[0];
        }
        return sLocalName != null ? sLocalName : "Neighbor";
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
     * Automatically falls back to local session if Firebase is unconfigured.
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
            // Firebase is not configured on this device -> provision local demo user session
            String uid = "usr_local_" + Math.abs(email.trim().hashCode());
            String name = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : email.split("@")[0];
            setLocalSession(uid, email.trim(), name);
            callback.onSuccess(null);
        }
    }

    /**
     * Signs in an existing user with email and password.
     * Automatically falls back to local session if Firebase is unconfigured.
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
            // Firebase is not configured on this device -> sign in as local demo user
            String uid = "usr_local_" + Math.abs(email.trim().hashCode());
            String name = (sLocalName != null && email.trim().equalsIgnoreCase(sLocalEmail))
                    ? sLocalName
                    : email.split("@")[0];
            setLocalSession(uid, email.trim(), name);
            callback.onSuccess(null);
        }
    }

    /**
     * Signs in or registers a user using a Google ID token.
     */
    public static void signInWithGoogle(String idToken, final AuthCallback callback) {
        if (idToken == null || idToken.trim().isEmpty()) {
            callback.onFailure("Invalid Google ID Token.");
            return;
        }

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            auth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                    .addOnFailureListener(e -> callback.onFailure(translateAuthException(e)));
        } catch (IllegalStateException e) {
            // Local fallback if Firebase not configured
            String uid = "usr_google_local_" + System.currentTimeMillis();
            setLocalSession(uid, "google.user@example.com", "Google Neighbor");
            callback.onSuccess(null);
        }
    }

    /**
     * Signs out the currently authenticated user.
     */
    public static void signOut() {
        clearLocalSession();
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
