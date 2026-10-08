package com.companyos.backend.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * GoogleTokenVerifierService — server-side verification of Google ID tokens.
 *
 * SECURITY CONTRACT
 * -----------------
 * The frontend uses Google Identity Services to obtain a Google ID Token (a
 * signed OpenID Connect JWT). This service validates that token by:
 *
 *   1. Checking the cryptographic signature against Google's published public keys
 *      (fetched from https://www.googleapis.com/oauth2/v3/certs). If the
 *      signature is invalid, the token is rejected immediately.
 *
 *   2. Verifying the `aud` (audience) claim equals our GOOGLE_CLIENT_ID.
 *      This prevents tokens issued for other apps being accepted here.
 *
 *   3. Verifying the `iss` (issuer) is "accounts.google.com" or
 *      "https://accounts.google.com".
 *
 *   4. Verifying the `exp` (expiry) has not passed.
 *
 * Only after all checks pass does this service return a GoogleUserInfo.
 * The client secret is NOT used in this flow — ID token verification uses
 * Google's public keys, which are publicly available.
 *
 * NOTE: If GOOGLE_CLIENT_ID is not configured (e.g. during early development),
 * the verify() method will throw IllegalStateException to prevent silent failures.
 */
@Service
public class GoogleTokenVerifierService {

    @Value("${google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        if (googleClientId == null || googleClientId.isBlank()) {
            // Verifier not initialised — verify() will throw on any call.
            // This avoids breaking startup when the env var is not yet set.
            return;
        }
        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    /**
     * Verify a Google ID token received from the frontend and extract its claims.
     *
     * @param idTokenString the raw ID token string from Google Identity Services
     * @return GoogleUserInfo populated with verified claims
     * @throws IllegalStateException  if GOOGLE_CLIENT_ID is not configured
     * @throws IllegalArgumentException if the token is invalid, expired, has the wrong
     *                                  audience, or cannot be verified
     */
    public GoogleUserInfo verify(String idTokenString) {
        if (verifier == null) {
            throw new IllegalStateException(
                "Google OAuth is not configured. " +
                "Set the GOOGLE_CLIENT_ID environment variable and restart the backend.");
        }

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            // Covers network errors fetching Google's public keys and parsing errors.
            throw new IllegalArgumentException(
                "Google authentication failed: could not verify token. " +
                "Please try again.", e);
        }

        if (idToken == null) {
            // verify() returns null when signature/audience/expiry check fails.
            throw new IllegalArgumentException(
                "Google authentication failed: invalid or expired token. Please sign in again.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        return new GoogleUserInfo(
            payload.getSubject(),                              // "sub" — stable Google user ID
            payload.getEmail(),                               // verified email
            Boolean.TRUE.equals(payload.getEmailVerified()), // must be true
            (String) payload.get("name"),                    // display name
            (String) payload.get("picture")                  // avatar URL (may be null)
        );
    }
}
