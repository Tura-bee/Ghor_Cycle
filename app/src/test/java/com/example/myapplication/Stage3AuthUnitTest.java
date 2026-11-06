package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Stage 3 Firebase Authentication logic:
 * 1. Email format validation.
 * 2. Password length requirements (minimum 6 characters).
 * 3. Human-readable translation of Firebase Authentication exceptions.
 * 4. Auth contract consistency across Splash, Login, Register, Profile, and AddItem.
 */
public class Stage3AuthUnitTest {

    @Test
    public void testEmailValidationLogic() {
        // Valid emails
        assertTrue(FirebaseAuthService.isValidEmail("user@ghorcycle.com"));
        assertTrue(FirebaseAuthService.isValidEmail("first.last@domain.co"));
        assertTrue(FirebaseAuthService.isValidEmail("community_member123@sub.domain.org"));

        // Invalid emails
        assertFalse(FirebaseAuthService.isValidEmail(null));
        assertFalse(FirebaseAuthService.isValidEmail(""));
        assertFalse(FirebaseAuthService.isValidEmail("   "));
        assertFalse(FirebaseAuthService.isValidEmail("not-an-email"));
        assertFalse(FirebaseAuthService.isValidEmail("missingdomain@"));
        assertFalse(FirebaseAuthService.isValidEmail("@missingusername.com"));
    }

    @Test
    public void testPasswordValidationLogic() {
        // Valid passwords (>= 6 chars)
        assertTrue(FirebaseAuthService.isValidPassword("123456"));
        assertTrue(FirebaseAuthService.isValidPassword("secret@2026"));
        assertTrue(FirebaseAuthService.isValidPassword("StrongP@ssw0rd!"));

        // Invalid passwords (< 6 chars or empty)
        assertFalse(FirebaseAuthService.isValidPassword(null));
        assertFalse(FirebaseAuthService.isValidPassword(""));
        assertFalse(FirebaseAuthService.isValidPassword("   "));
        assertFalse(FirebaseAuthService.isValidPassword("12345"));
        assertFalse(FirebaseAuthService.isValidPassword("abc"));
    }

    @Test
    public void testExceptionTranslationProducesReadableMessages() {
        // Weak password
        String weakMsg = FirebaseAuthService.translateAuthException(
                new FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "Password too weak", "length"));
        assertTrue(weakMsg.toLowerCase().contains("weak"));

        // Invalid credentials / bad password
        String invalidCredMsg = FirebaseAuthService.translateAuthException(
                new FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Invalid credential"));
        assertTrue(invalidCredMsg.toLowerCase().contains("invalid") || invalidCredMsg.toLowerCase().contains("password"));

        // User already exists
        String collisionMsg = FirebaseAuthService.translateAuthException(
                new FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "User exists"));
        assertTrue(collisionMsg.toLowerCase().contains("already exists"));

        // User not found
        String notFoundMsg = FirebaseAuthService.translateAuthException(
                new FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "User not found"));
        assertTrue(notFoundMsg.toLowerCase().contains("no registered account") || notFoundMsg.toLowerCase().contains("not found"));

        // Null exception
        String nullMsg = FirebaseAuthService.translateAuthException(null);
        assertNotNull(nullMsg);
        assertFalse(nullMsg.isEmpty());
    }

    @Test
    public void testExtraKeysIntegrityAcrossAuthScreens() {
        assertNotNull(LoginActivity.EXTRA_USER_NAME);
        assertNotNull(ProfileActivity.EXTRA_PROFILE_NAME);
        assertEquals("extra_user_name", LoginActivity.EXTRA_USER_NAME);
        assertEquals("com.ghorcycle.extra.PROFILE_NAME", ProfileActivity.EXTRA_PROFILE_NAME);
    }
}
