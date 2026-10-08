package com.companyos.backend.security;

/**
 * Holds the verified claims extracted from a Google ID token.
 *
 * This is an internal data-transfer record — it is ONLY populated after the
 * GoogleTokenVerifierService has successfully verified the token signature,
 * audience, and expiry. Never instantiate this from unverified frontend input.
 */
public class GoogleUserInfo {

    /** Google subject ID ("sub") — stable, unique identifier per Google account. */
    private final String subject;

    /** User's primary Google email address. */
    private final String email;

    /** Whether Google has verified this email address. Must be true before we trust it. */
    private final boolean emailVerified;

    /** Display name from Google profile. */
    private final String name;

    /** Google profile picture URL. Optional — may be null. */
    private final String pictureUrl;

    public GoogleUserInfo(String subject, String email, boolean emailVerified,
                          String name, String pictureUrl) {
        this.subject       = subject;
        this.email         = email;
        this.emailVerified = emailVerified;
        this.name          = name;
        this.pictureUrl    = pictureUrl;
    }

    public String  getSubject()       { return subject; }
    public String  getEmail()         { return email; }
    public boolean isEmailVerified()  { return emailVerified; }
    public String  getName()          { return name; }
    public String  getPictureUrl()    { return pictureUrl; }
}
