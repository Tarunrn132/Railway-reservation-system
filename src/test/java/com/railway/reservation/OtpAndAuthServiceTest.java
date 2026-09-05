package com.railway.reservation;

import com.railway.reservation.dto.*;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.service.AuthService;
import com.railway.reservation.service.OtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OtpAndAuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Test
    @DisplayName("Phone OTP: Successful OTP generation and dispatch")
    void testSendPhoneOtpSuccess() {
        PhoneOtpRequest req = new PhoneOtpRequest("9876543210");
        assertDoesNotThrow(() -> authService.sendPhoneOtp(req));
    }

    @Test
    @DisplayName("Phone OTP: Rate limiting enforces cooldown between consecutive OTP requests")
    void testOtpCooldownRateLimiting() {
        PhoneOtpRequest req = new PhoneOtpRequest("9876543211");
        authService.sendPhoneOtp(req);

        // Immediate second attempt should trigger cooldown BadRequestException
        assertThrows(BadRequestException.class, () -> authService.sendPhoneOtp(req));
    }

    @Test
    @DisplayName("Phone OTP: Verification fails for invalid OTP")
    void testVerifyInvalidOtpFails() {
        PhoneOtpRequest req = new PhoneOtpRequest("9876543212");
        authService.sendPhoneOtp(req);

        PhoneOtpVerifyRequest verifyReq = new PhoneOtpVerifyRequest("9876543212", "000000");
        assertThrows(BadRequestException.class, () -> authService.verifyPhoneOtpAndLogin(verifyReq));
    }

    @Test
    @DisplayName("Password Reset: Request reset for registered email")
    void testForgotPasswordSuccess() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("tarun@example.com");
        assertDoesNotThrow(() -> authService.forgotPassword(req));
    }

    @Test
    @DisplayName("Password Reset: Reset password with valid token and new password")
    void testResetPasswordSuccess() {
        ResetPasswordRequest req = new ResetPasswordRequest("tarun@example.com", "123456", "newSecurePassword123");
        assertDoesNotThrow(() -> authService.resetPassword(req));

        // Verify login works with new password
        LoginRequest loginReq = new LoginRequest("tarun@example.com", "newSecurePassword123");
        AuthResponse auth = authService.login(loginReq);
        assertNotNull(auth);
        assertEquals("tarun@example.com", auth.getEmail());
    }

    @Test
    @DisplayName("Google Login: Fails cleanly when Google OAuth credentials are not configured")
    void testGoogleLoginUnconfigured() {
        GoogleAuthRequest req = new GoogleAuthRequest("test_token_123", "google_passenger@example.com", "Google User");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.googleLogin(req));
        assertTrue(ex.getMessage().contains("Google Login is not configured"));
    }
}
