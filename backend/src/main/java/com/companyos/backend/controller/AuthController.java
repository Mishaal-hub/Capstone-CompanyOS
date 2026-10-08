package com.companyos.backend.controller;

import com.companyos.backend.dto.ForgotPasswordRequest;
import com.companyos.backend.dto.LoginRequest;
import com.companyos.backend.dto.LoginResponse;
import com.companyos.backend.dto.RegisterRequest;
import com.companyos.backend.dto.ResetPasswordRequest;
import com.companyos.backend.dto.VerifyResetOtpRequest;
import com.companyos.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // REGISTER
    // POST /api/auth/register
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(message);
    }

    // =========================================================
    // VERIFY EMAIL
    // POST /api/auth/verify?username=email&code=123456
    // =========================================================

    @PostMapping("/verify")
    public ResponseEntity<String> verify(
            @RequestParam String username,
            @RequestParam String code) {

        String message =
                authService.verifyEmail(username, code);

        return ResponseEntity.ok(message);
    }

    // =========================================================
    // RESEND VERIFICATION
    // POST /api/auth/resend-verification?username=email
    // =========================================================

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam String username) {

        String message =
                authService.resendVerification(username);

        return ResponseEntity.ok(message);
    }

    // =========================================================
    // LOGIN
    // POST /api/auth/login
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GOOGLE LOGIN
    // GET /api/auth/google
    // =========================================================

    @GetMapping("/google")
    public void initiateGoogleOAuth(
            HttpServletResponse response) throws Exception {

        String authUrl =
                authService.initiateGoogleOAuth();

        response.sendRedirect(authUrl);
    }

    // =========================================================
    // GOOGLE CALLBACK
    // GET /api/auth/google/callback?code=...
    // =========================================================

    @GetMapping("/google/callback")
    public void googleCallback(
            @RequestParam(name = "code") String authorizationCode,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws Exception {

        String redirectPath =
                authService.handleGoogleCallback(
                        authorizationCode,
                        servletRequest
                );

        String frontendBase =
                "http://localhost:5173";

        String fullRedirectUrl =
                frontendBase + redirectPath;

        servletResponse.sendRedirect(fullRedirectUrl);
    }

    // =========================================================
    // GOOGLE OTP VERIFY
    // POST /api/auth/google/verify-otp
    // =========================================================

    @PostMapping("/google/verify-otp")
    public ResponseEntity<LoginResponse> verifyGoogleOtp(
            @RequestBody Map<String, String> payload) {

        String code = payload.get("code");
        String otp = payload.get("otp");

        LoginResponse response =
                authService.verifyGoogleOtp(code, otp);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GOOGLE OTP RESEND
    // POST /api/auth/google/resend-otp
    // =========================================================

    @PostMapping("/google/resend-otp")
    public ResponseEntity<String> resendGoogleOtp(
            @RequestBody Map<String, String> payload) {

        String code = payload.get("code");

        String message =
                authService.resendGoogleOtp(code);

        return ResponseEntity.ok(message);
    }

    // =========================================================
    // GOOGLE CODE EXCHANGE
    // POST /api/auth/google/exchange?code=...
    // =========================================================

    @PostMapping("/google/exchange")
    public ResponseEntity<LoginResponse> exchangeGoogleCode(
            @RequestParam String code) {

        LoginResponse response =
                authService.exchangeGoogleCode(code);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // FORGOT PASSWORD
    // POST /api/auth/forgot-password
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String message =
                authService.forgotPassword(
                        request.getEmail()
                );

        return ResponseEntity.ok(message);
    }

    // =========================================================
    // VERIFY RESET OTP
    // POST /api/auth/verify-reset-otp
    // =========================================================

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {

        String message =
                authService.verifyResetOtp(
                        request.getEmail(),
                        request.getCode()
                );

        return ResponseEntity.ok(message);
    }

    // =========================================================
    // RESET PASSWORD
    // POST /api/auth/reset-password
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String message =
                authService.resetPassword(
                        request.getEmail(),
                        request.getCode(),
                        request.getNewPassword()
                );

        return ResponseEntity.ok(message);
    }
}