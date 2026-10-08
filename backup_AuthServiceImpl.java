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

    // In-memory one-time exchange code store (MVP/local development).
    // Key: the random code string; Value: expiry time + user info + consumed flag.
    // This is NOT production-grade for multiple backend instances;
    // it is documented as a temporary measure that can be replaced by Redis.
    private final Map<String, ExchangeCode> exchangeCodeStore = new ConcurrentHashMap<>();

    // Exchange code expires 120 seconds after creation
    private static final long EXPIRATION_SECONDS = 120;

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
        // Remove any codes that may have survived from a previous session
        // (e.g. if the app was restarted without consuming them all).
        LocalDateTime now = LocalDateTime.now();
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
            throw new RuntimeException(
                    "Account created but verification email could not be sent. " +
                            "Please use POST /api/auth/resend-verification to request a new code.");
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
            return completeGoogleLogin(user, googleUser);
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

            return completeGoogleLogin(user, googleUser);
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
        return completeGoogleLogin(savedUser, googleUser);
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
     * Simple holder for exchange code metadata.
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

    // ── Existing utilities ──────────────────────────────────────────────────

    private String generateVerificationCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}