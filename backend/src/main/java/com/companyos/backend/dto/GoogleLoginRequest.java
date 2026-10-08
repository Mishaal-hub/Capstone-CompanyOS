package com.companyos.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/google.
 *
 * The frontend uses Google Identity Services (GIS) to obtain a Google ID Token
 * (an OpenID Connect JWT signed by Google). That token is sent here; the backend
 * verifies it cryptographically using Google's public keys — the client secret
 * is NEVER sent to or from the frontend.
 */
public class GoogleLoginRequest {

    /**
     * The Google ID Token returned by Google Identity Services on the frontend.
     * This is a signed JWT; the backend will verify its signature and claims
     * before trusting any of its contents.
     */
    @NotBlank(message = "Google ID token must not be blank")
    private String idToken;

    public GoogleLoginRequest() {}

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken()               { return idToken; }
    public void setIdToken(String idToken)   { this.idToken = idToken; }
}
