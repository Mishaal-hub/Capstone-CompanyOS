package com.companyos.backend.service;

import com.companyos.backend.dto.LoginRequest;
import com.companyos.backend.dto.LoginResponse;
import com.companyos.backend.dto.RegisterRequest;

public interface AuthService {

    // =========================
    // Registration
    // =========================

    String register(RegisterRequest request);

    // =========================
    // Email Verification
    // =========================

    String verifyEmail(String username, String code);

    String resendVerification(String username);

    // =========================
    // Normal Login
    // =========================

    LoginResponse login(LoginRequest request);

    // =========================
    // Google OAuth
    // =========================

    String initiateGoogleOAuth();

    String handleGoogleCallback(
            String authorizationCode,
            jakarta.servlet.http.HttpServletRequest servletRequest
    );

    LoginResponse exchangeGoogleCode(String code);

    LoginResponse verifyGoogleOtp(
            String code,
            String otp
    );

    String resendGoogleOtp(String code);

    // =========================
    // Password Reset
    // =========================

    /**
     * Send a password reset OTP to the user's email.
     */
    String forgotPassword(String email);

    /**
     * Verify the password reset OTP.
     */
    String verifyResetOtp(
            String email,
            String code
    );

    /**
     * Reset the user's password after OTP verification.
     */
    String resetPassword(
            String email,
            String code,
            String newPassword
    );
}