package com.companyos.backend.service.impl;

import com.companyos.backend.dto.GoogleLoginRequest;
import com.companyos.backend.dto.LoginRequest;
import com.companyos.backend.dto.LoginResponse;
import com.companyos.backend.dto.RegisterRequest;
import com.companyos.backend.entity.Organization;
import com.companyos.backend.entity.Role;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.OrganizationRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.service.AuthService;
import com.companyos.backend.service.EmailService;
import com.companyos.backend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import com.companyos.backend.security.GoogleUserInfo;
import com.companyos.backend.security.GoogleTokenVerifierService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserRegistrationHelper registrationHelper;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    private final SecureRandom secureRandom = new SecureRandom();

    // In-memory session store for Google OAuth OTP verification.
    // Key: cryptographically random session ID; Value: OTP metadata.
    // Session IDs are generated using SecureRandom and are never derived from the OTP.
    // OTPs are hashed before storage using BCrypt; the raw OTP is only sent via email.
    private final Map<String, OtpCode> sessionsBySessionId = new ConcurrentHashMap<>();
// In-memory password reset OTP sessions.
// Key: user's email.
// OTP is stored only as a BCrypt hash.
private final Map<String, PasswordResetOtp> passwordResetSessions =
        new ConcurrentHashMap<>();
    // In-memory exchange code store (backward compatibility).
    private final Map<String, ExchangeCode> exchangeCodeStore = new ConcurrentHashMap<>();

    // Exchange code expires 120 seconds after creation
    private static final long EXPIRATION_SECONDS = 120;

    // OTP expires 5 minutes after creation
    private static final long OTP_EXPIRATION_MINUTES = 5;

    // Maximum incorrect OTP attempts before lockout
    private static final int OTP_MAX_ATTEMPTS = 5;

    // Resend OTP cooldown in seconds
    private static final long OTP_RESEND_COOLDOWN_SECONDS = 60;

    public AuthServiceImpl(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtUtil jwtUtil,
            UserRegistrationHelper registrationHelper,
            GoogleTokenVerifierService googleTokenVerifierService) {

        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.registrationHelper = registrationHelper;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostConstruct
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        sessionsBySessionId.entrySet().removeIf(entry -> {
            OtpCode otp = entry.getValue();
            return otp.isConsumed() || otp.getExpiry().isBefore(now);
        });
        exchangeCodeStore.entrySet().removeIf(entry -> {
            ExchangeCode code = entry.getValue();
            return code.isConsumed() || code.getExpiry().isBefore(now);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registration / Verification (unchanged from original)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String register(RegisterRequest request) {
        String email = request.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null && !request.getFullName().isBlank()
                ? request.getFullName().trim()
                : email.split("@")[0]);

        String orgName = request.getOrganizationName();
        if (orgName == null || orgName.isBlank()) {
            orgName = user.getFullName() + "'s Workspace";
        }

        user.setRole(Role.OWNER.name());
        user.setStatus("ACTIVE");
        user.setVerified(false);
        user.setVerificationCode(generateVerificationCode());
        user.setAuthProvider("LOCAL");

        String slug = orgName.toLowerCase().replaceAll("[^a-z0-9]", "-") + "-" + secureRandom.nextInt(10000);
        Organization organization = new Organization(orgName, slug);
        organization.setEmail(email);

        User savedUser = registrationHelper.saveUserWithOrganization(user, organization);

        try {
            emailService.sendVerificationEmail(email, savedUser.getVerificationCode());
        } catch (Exception e) {
            return "Registration successful. Account created but verification email could not be sent. " +
                    "Please use POST /api/auth/resend-verification to request a new code.";
        }

        return "Registration successful. A verification code has been sent to " + email + ".";
    }

    @Override
    @Transactional
    public String verifyEmail(String username, String code) {
        String email = username.trim().toLowerCase();

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        if (user.isVerified()) {
            return "This account is already verified. You can log in.";
        }

        if (user.getVerificationCode() == null ||
            !user.getVerificationCode().equals(code.trim())) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        return "Email verified successfully. You can now log in.";
    }

    @Override
    @Transactional
    public String resendVerification(String username) {
        String email = username.trim().toLowerCase();

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        if (user.isVerified()) {
            return "This account is already verified. You can log in.";
        }

        String newCode = generateVerificationCode();
        user.setVerificationCode(newCode);
        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(email, newCode);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not send verification email. " +
                            "Check your email configuration and try again.");
        }

        return "A new verification code has been sent to " + email + ".";
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getUsername().trim().toLowerCase();

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if ("LOCKED".equals(user.getStatus())) {
            throw new IllegalStateException("This account has been locked. Please contact support.");
        }

        if ("INACTIVE".equals(user.getStatus())) {
            throw new IllegalStateException("This account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!user.isVerified()) {
            throw new IllegalStateException(
                    "Please verify your email before logging in. " +
                            "Check your inbox or use POST /api/auth/resend-verification.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String orgName = "CompanyOS Workspace";
        if (user.getOrganizationId() != null) {
            orgName = organizationRepository.findById(user.getOrganizationId())
                    .map(Organization::getName)
                    .orElse("CompanyOS Workspace");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole(), user.getOrganizationId());

        return new LoginResponse(
            user.getUserId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole(),
            user.getOrganizationId(),
            orgName,
            token
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Google OAuth 2.0 / OpenID Connect — one-time exchange code flow
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String initiateGoogleOAuth() {
        // Build the Google OAuth 2.0 authorization URL.
        // The browser should be redirected to this URL.
        // After the user authorizes, Google will redirect back to
        //   /api/auth/google/callback?code=AUTHORIZATION_CODE
        //
        // The caller (typically a controller) should redirect the browser to
        // this URL. Do NOT send the authorization code or any Google credentials
        // to the frontend.
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth is not configured. Set the GOOGLE_CLIENT_ID environment variable and restart the backend.");
        }

        String redirectUri = System.getenv("GOOGLE_REDIRECT_URI");
        if (redirectUri == null || redirectUri.isBlank()) {
            redirectUri = "http://localhost:8080/api/auth/google/callback";
        }

        String scope = "openid profile email";

        // Google's OAuth 2.0 endpoint
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope +
                "&access_type=offline" +
                "&prompt=consent";

        return authUrl;
    }

    @Override
    @Transactional
    public String handleGoogleCallback(String authorizationCode, HttpServletRequest servletRequest) {
        // 1. Exchange the authorization code for a Google ID token using a simple HTTP POST.
        //    We use Google's token endpoint with the client credentials.
        // 2. Verify the ID token using Google's official public keys via GoogleTokenVerifierService.
        // 3. Extract verified user info.
        // 4. Perform account handling cases A-D.
        // 5. Store a one-time exchange code.
        // 6. Return a redirect path the frontend can read (?google_code=CODE).

        // Exchange authorization code for tokens from Google
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        String redirectUri = "http://localhost:8080/api/auth/google/callback";

        if (googleClientId == null || googleClientId.isBlank() ||
                googleClientSecret == null || googleClientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth is not fully configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.");
        }

        // Simple HTTP POST to exchange authorization code for ID token
        String idTokenString = exchangeCodeForIdToken(authorizationCode, googleClientId, googleClientSecret, redirectUri);

        // 3. Verify the ID token using Google's public keys.
        GoogleUserInfo googleUser = googleTokenVerifierService.verify(idTokenString);

        // 4. Google must have verified the email. Reject unverified emails.
        if (!googleUser.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Your Google account email is not verified. Please verify it with Google first.");
        }

        String email = googleUser.getEmail().trim().toLowerCase();
        String googleSubject = googleUser.getSubject(); // stable Google user ID ("sub")

        // 5. Account handling cases A-D:

        // CASE B: Google identity already belongs to a CompanyOS user (by googleId)
        Optional<User> byGoogleId = userRepository.findByGoogleId(googleSubject);
        if (byGoogleId.isPresent()) {
            User user = byGoogleId.get();
            user.setGoogleId(googleSubject);
            if ("LOCAL".equals(user.getAuthProvider())) {
                user.setAuthProvider("LOCAL,GOOGLE");
            }
            user.setVerified(true);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return "/?google_code=" + storeGoogleOtp(user.getUserId(), user.getGoogleId(), email);
        }

        // CASE C: Verified Google email matches an existing email/password account
        Optional<User> byEmail = userRepository.findByUsername(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();

            if ("LOCKED".equals(user.getStatus())) {
                throw new IllegalStateException("This account has been locked. Please contact support.");
            }
            if ("INACTIVE".equals(user.getStatus())) {
                throw new IllegalStateException("This account is inactive. Please contact support.");
            }

            // Link Google identity to existing account
            user.setGoogleId(googleSubject);
            // Update authProvider: if currently "LOCAL", add "GOOGLE"
            if ("LOCAL".equals(user.getAuthProvider())) {
                user.setAuthProvider("LOCAL,GOOGLE");
            }
            // If already has Google, keep as-is
            if (user.getAvatarUrl() == null && googleUser.getPictureUrl() != null) {
                user.setAvatarUrl(googleUser.getPictureUrl());
            }

            user.setVerified(true); // Google verified the email
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return "/?google_code=" + storeGoogleOtp(user.getUserId(), user.getGoogleId(), email);
        }

        // CASE A: No existing CompanyOS user for this Google identity/email
        // Create new CompanyOS user from Google info
        User newUser = new User();
        newUser.setUsername(email);
        newUser.setPassword(null); // Google-only account: no password
        newUser.setFullName(googleUser.getName() != null ? googleUser.getName().trim() : email.split("@")[0]);
        newUser.setGoogleId(googleSubject);
        newUser.setAuthProvider("GOOGLE");
        newUser.setVerified(true); // email verified by Google
        newUser.setStatus("ACTIVE");
        newUser.setRole(Role.OWNER.name());
        newUser.setAvatarUrl(googleUser.getPictureUrl());

        // Create default organization for the new user
        String orgName = newUser.getFullName() + "'s Workspace";
        String slug = orgName.toLowerCase().replaceAll("[^a-z0-9]", "-") + "-" + secureRandom.nextInt(10000);
        Organization organization = new Organization(orgName, slug);
        organization.setEmail(email);

        User savedUser = registrationHelper.saveUserWithOrganization(newUser, organization);
        return "/?google_code=" + storeGoogleOtp(savedUser.getUserId(), savedUser.getGoogleId(), email);
    }

    @Override
    public LoginResponse verifyGoogleOtp(String sessionId, String otp) {
        OtpCode otpCode = sessionsBySessionId.get(sessionId);
        if (otpCode == null) {
            throw new IllegalArgumentException("Invalid or expired session. Please start a new Google sign-in.");
        }

        if (otpCode.isConsumed()) {
            throw new IllegalArgumentException("This OTP has already been used. Please start a new sign-in.");
        }

        if (otpCode.getExpiry().isBefore(LocalDateTime.now())) {
            sessionsBySessionId.remove(sessionId);
            throw new IllegalArgumentException("OTP expired. Please request a new OTP.");
        }

        if (otpCode.getAttempts() >= OTP_MAX_ATTEMPTS) {
            throw new IllegalStateException("Too many attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(otp.trim(), otpCode.getHashedOtp())) {
            otpCode.incrementAttempts();
            throw new IllegalArgumentException("Invalid OTP.");
        }

        // OTP verified — consume it (one-time use)
        otpCode.setConsumed(true);
        sessionsBySessionId.remove(sessionId);

        Long userId = otpCode.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found associated with this sign-in."));

        String orgName = resolveOrgName(user);
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole(), user.getOrganizationId());

        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getOrganizationId(),
                orgName,
                token
        );
    }

    /**
     * Resend the OTP for a Google sign-in session.
     * Enforces a cooldown period between resend requests.
     *
     * @param code the one-time code from the Google callback URL
     * @return confirmation message
     */
    public String resendGoogleOtp(String sessionId) {
        OtpCode otpCode = sessionsBySessionId.get(sessionId);
        if (otpCode == null) {
            throw new IllegalArgumentException("Invalid or expired session. Please start a new Google sign-in.");
        }

        if (otpCode.isConsumed()) {
            throw new IllegalArgumentException("This OTP has already been used. Please start a new sign-in.");
        }

        LocalDateTime lastResend = otpCode.getLastResend();
        if (lastResend != null &&
            LocalDateTime.now().isBefore(lastResend.plusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
            long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    lastResend.plusSeconds(OTP_RESEND_COOLDOWN_SECONDS));
            throw new IllegalStateException(
                    "Please wait before requesting another OTP. Try again in " + remaining + " seconds.");
        }

        // Generate new OTP, invalidate old one
        String newOtp = generateVerificationCode();
        String hashedOtp = passwordEncoder.encode(newOtp);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        // Update session with new OTP (same sessionId)
        otpCode.setHashedOtp(hashedOtp);
        otpCode.setExpiry(expiry);
        otpCode.setAttempts(0);
        otpCode.setConsumed(false);
        otpCode.setLastResend(LocalDateTime.now());
        sessionsBySessionId.put(sessionId, otpCode);

        // Send new OTP email
        emailService.sendGoogleOtp(otpCode.getEmail(), newOtp);

        return "A new OTP has been sent to " + otpCode.getEmail() + ".";
    }

    /**
     * Store a Google OTP for the authenticated user.
     * Generates a cryptographically random session ID, hashes the OTP with BCrypt,
     * sets a 5-minute expiry, and stores the session in sessionsBySessionId.
     * The raw OTP is only sent via email — never logged or stored as a key.
     *
     * @param userId    the CompanyOS user ID
     * @param googleId  the Google subject ID
     * @param email     the verified Google email
     * @return the cryptographically random session ID (safe to put in URL)
     */
    private String storeGoogleOtp(Long userId, String googleId, String email) {
        User user = userRepository.findById(userId).orElseThrow();
        String otp = generateVerificationCode();
        String hashedOtp = passwordEncoder.encode(otp);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        // Generate cryptographically random session ID — never derived from OTP
        byte[] sessionBytes = new byte[24];
        secureRandom.nextBytes(sessionBytes);
        String sessionId = Base64.getUrlEncoder().withoutPadding().encodeToString(sessionBytes);

        OtpCode otpCode = new OtpCode(userId, googleId, email, hashedOtp, expiry, sessionId);
        sessionsBySessionId.put(sessionId, otpCode);

        // Never log the raw OTP — only send it via email.
        emailService.sendGoogleOtp(email, otp);

        return sessionId;
    }

    /**
     * Generate a cryptographically secure, random 6-digit code.
     *
     * @return a 6-digit string
     */
    private String generateOtpCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    /**
     * Exchange the Google authorization code for an ID token using a simple HTTP POST.
     * This avoids the Google API client library compilation issues while still
     * obtaining a verified ID token from Google's token endpoint.
     */
    private String exchangeCodeForIdToken(
            String authorizationCode,
            String clientId,
            String clientSecret,
            String redirectUri) {

        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(
                    "https://oauth2.googleapis.com/token").openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String params = "grant_type=authorization_code" +
                    "&code=" + authorizationCode +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&redirect_uri=" + redirectUri;
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(params.getBytes("UTF-8"));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IllegalArgumentException(
                        "Google token exchange failed: HTTP " + responseCode);
            }

            java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
            String responseBody = scanner.next();
            scanner.close();

            // Parse the JSON response to extract the id_token
            // Use simple JSON parsing since we only need the id_token
            String idToken = null;
            // Extract id_token from the JSON response
            String[] tokens = responseBody.split(",");
            for (String token : tokens) {
                if (token.contains("\"id_token\"")) {
                    // Extract the value after : and remove quotes
                    String[] parts = token.split(":");
                    if (parts.length > 1) {
                        idToken = parts[1].replace("\"", "").trim();
                    }
                }
            }

            if (idToken == null) {
                throw new IllegalArgumentException(
                        "Failed to obtain id_token from Google token response");
            }

            return idToken;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Google token exchange failed: " + e.getMessage(), e);
        }
    }

    @Override
    public LoginResponse exchangeGoogleCode(String code) {
        // Validate and consume the one-time exchange code.
        ExchangeCode exchangeCode = exchangeCodeStore.remove(code);
        if (exchangeCode == null) {
            throw new IllegalArgumentException(
                    "Invalid or already-consumed exchange code. It may have expired or been used already.");
        }

        // Code is already consumed (removed from map), now look up the user
        Long userId = exchangeCode.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found associated with exchange code."));

        // Generate the same CompanyOS JWT as normal login
        String orgName = "CompanyOS Workspace";
        if (user.getOrganizationId() != null) {
            orgName = organizationRepository.findById(user.getOrganizationId())
                    .map(Organization::getName)
                    .orElse("CompanyOS Workspace");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole(), user.getOrganizationId());

        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getOrganizationId(),
                orgName,
                token
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shared completion logic for all Google login paths:
     * persist the user, resolve org name, and issue a CompanyOS JWT.
     */
    private LoginResponse completeGoogleLogin(User user, GoogleUserInfo googleUser) {
        user.setLastLogin(LocalDateTime.now());
        User saved = userRepository.save(user);

        String orgName = resolveOrgName(saved);
        String token = jwtUtil.generateToken(
                saved.getUserId(), saved.getUsername(), saved.getRole(), saved.getOrganizationId());

        return new LoginResponse(
                saved.getUserId(),
                saved.getUsername(),
                saved.getFullName(),
                saved.getRole(),
                saved.getOrganizationId(),
                orgName,
                token
        );
    }

    private String resolveOrgName(User user) {
        if (user.getOrganizationId() != null) {
            return organizationRepository.findById(user.getOrganizationId())
                    .map(Organization::getName)
                    .orElse("CompanyOS Workspace");
        }
        return "CompanyOS Workspace";
    }

    /**
     * Generate a cryptographically secure, random one-time exchange code.
     *
     * @return a 32-character base64-encoded code
     */
    private String generateExchangeCode() {
        byte[] randomBytes = new byte[24]; // 24 bytes → ~32 base64 chars
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Store a one-time exchange code associated with the Google-authenticated user.
     * The code expires after 120 seconds and is single-use.
     *
     * @param userId       the CompanyOS user ID
     * @param googleId     the Google subject ID (may be null for new users)
     * @param email        the verified Google email
     * @return the generated exchange code string
     */
    private String storeExchangeCode(Long userId, String googleId, String email) {
        String code = generateExchangeCode();
        LocalDateTime expiry = LocalDateTime.now().plus(EXPIRATION_SECONDS, ChronoUnit.SECONDS);
        ExchangeCode ec = new ExchangeCode(userId, googleId, email, expiry);
        exchangeCodeStore.put(code, ec);
        return code;
    }

    /**
     * Simple holder for exchange code metadata (backward compatibility).
     */
    private static class ExchangeCode {
        private final Long userId;
        private final String googleId;
        private final String email;
        private final LocalDateTime expiry;
        private boolean consumed;

        public ExchangeCode(Long userId, String googleId, String email, LocalDateTime expiry) {
            this.userId = userId;
            this.googleId = googleId;
            this.email = email;
            this.expiry = expiry;
            this.consumed = false;
        }

        public Long getUserId() { return userId; }
        public String getGoogleId() { return googleId; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiry() { return expiry; }
        public boolean isConsumed() { return consumed; }
        public void setConsumed() { this.consumed = true; }
    }

    /**
     * Simple holder for Google OTP session metadata.
     * The OTP is stored as a BCrypt hash. The raw OTP is only sent via email.
     * The sessionId is a cryptographically random identifier used to look up
     * the pending OTP from the frontend — never derived from the OTP itself.
     */
    private static class OtpCode {
        private final Long userId;
        private final String googleId;
        private final String email;
        private final String sessionId;
        private String hashedOtp;
        private LocalDateTime expiry;
        private int attempts;
        private boolean consumed;
        private LocalDateTime createdAt;
        private LocalDateTime lastResend;

        public OtpCode(Long userId, String googleId, String email, String hashedOtp, LocalDateTime expiry, String sessionId) {
            this.userId = userId;
            this.googleId = googleId;
            this.email = email;
            this.hashedOtp = hashedOtp;
            this.expiry = expiry;
            this.sessionId = sessionId;
            this.attempts = 0;
            this.consumed = false;
            this.createdAt = LocalDateTime.now();
            this.lastResend = null;
        }

        public Long getUserId() { return userId; }
        public String getGoogleId() { return googleId; }
        public String getEmail() { return email; }
        public String getSessionId() { return sessionId; }
        public String getHashedOtp() { return hashedOtp; }
        public void setHashedOtp(String hashedOtp) { this.hashedOtp = hashedOtp; }
        public LocalDateTime getExpiry() { return expiry; }
        public void setExpiry(LocalDateTime expiry) { this.expiry = expiry; }
        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        public void incrementAttempts() { this.attempts++; }
        public boolean isConsumed() { return consumed; }
        public void setConsumed(boolean consumed) { this.consumed = consumed; }
        public LocalDateTime getLastResend() { return lastResend; }
        public void setLastResend(LocalDateTime lastResend) { this.lastResend = lastResend; }
    }

    // ── Existing utilities ──────────────────────────────────────────────────

    private String generateVerificationCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }
    @Override
@Transactional
public String forgotPassword(String email) {

    String normalizedEmail = email.trim().toLowerCase();

    User user = userRepository.findByUsername(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException(
                    "No account found with this email address."));

    if ("LOCKED".equals(user.getStatus())) {
        throw new IllegalStateException(
                "This account has been locked. Please contact support.");
    }

    if ("INACTIVE".equals(user.getStatus())) {
        throw new IllegalStateException(
                "This account is inactive. Please contact support.");
    }

    String otp = generateVerificationCode();

    String hashedOtp = passwordEncoder.encode(otp);

    LocalDateTime expiry =
            LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

    PasswordResetOtp resetOtp =
            new PasswordResetOtp(hashedOtp, expiry);

    passwordResetSessions.put(normalizedEmail, resetOtp);

    try {
        emailService.sendPasswordResetOtp(normalizedEmail, otp);
    } catch (Exception e) {
        // Print the real email/SMTP error in the backend console
        // so configuration problems can be diagnosed.
        e.printStackTrace();

        passwordResetSessions.remove(normalizedEmail);

        throw new RuntimeException(
                "Could not send password reset email. " +
                "Check your email configuration and try again.");
    }

    return "A password reset code has been sent to " + normalizedEmail + ".";
}
@Override
public String verifyResetOtp(String email, String code) {

    String normalizedEmail = email.trim().toLowerCase();

    PasswordResetOtp resetOtp =
            passwordResetSessions.get(normalizedEmail);

    if (resetOtp == null) {
        throw new IllegalArgumentException(
                "No password reset request found. Please request a new code.");
    }

    if (resetOtp.isConsumed()) {
        throw new IllegalArgumentException(
                "This reset code has already been used.");
    }

    if (resetOtp.getExpiry().isBefore(LocalDateTime.now())) {
        passwordResetSessions.remove(normalizedEmail);

        throw new IllegalArgumentException(
                "Reset code expired. Please request a new code.");
    }

    if (resetOtp.getAttempts() >= OTP_MAX_ATTEMPTS) {
        passwordResetSessions.remove(normalizedEmail);

        throw new IllegalStateException(
                "Too many incorrect attempts. Please request a new code.");
    }

    if (!passwordEncoder.matches(
            code.trim(),
            resetOtp.getHashedOtp())) {

        resetOtp.incrementAttempts();

        throw new IllegalArgumentException(
                "Invalid reset code.");
    }

    resetOtp.setVerified(true);

    return "Reset code verified successfully.";
}

@Override
@Transactional
public String resetPassword(
        String email,
        String code,
        String newPassword) {

    String normalizedEmail = email.trim().toLowerCase();

    PasswordResetOtp resetOtp =
            passwordResetSessions.get(normalizedEmail);

    if (resetOtp == null) {
        throw new IllegalArgumentException(
                "No password reset request found. Please request a new code.");
    }

    if (resetOtp.isConsumed()) {
        throw new IllegalArgumentException(
                "This reset code has already been used.");
    }

    if (!resetOtp.isVerified()) {
        throw new IllegalArgumentException(
                "Please verify the reset code first.");
    }

    if (resetOtp.getExpiry().isBefore(LocalDateTime.now())) {
        passwordResetSessions.remove(normalizedEmail);

        throw new IllegalArgumentException(
                "Reset code expired. Please request a new code.");
    }

    if (!passwordEncoder.matches(
            code.trim(),
            resetOtp.getHashedOtp())) {

        throw new IllegalArgumentException(
                "Invalid reset code.");
    }

    User user = userRepository.findByUsername(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Account not found."));

    user.setPassword(passwordEncoder.encode(newPassword));

    userRepository.save(user);

    resetOtp.setConsumed(true);

    passwordResetSessions.remove(normalizedEmail);

    return "Password reset successfully. You can now log in.";
}
private static class PasswordResetOtp {

    private final String hashedOtp;
    private final LocalDateTime expiry;

    private int attempts;
    private boolean verified;
    private boolean consumed;

    public PasswordResetOtp(
            String hashedOtp,
            LocalDateTime expiry) {

        this.hashedOtp = hashedOtp;
        this.expiry = expiry;
        this.attempts = 0;
        this.verified = false;
        this.consumed = false;
    }

    public String getHashedOtp() {
        return hashedOtp;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
}