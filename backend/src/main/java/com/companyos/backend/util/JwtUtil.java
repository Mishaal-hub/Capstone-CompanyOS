package com.companyos.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * JwtUtil — creates and validates signed JWT tokens for CompanyOS multi-tenant authentication.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:default-companyos-dev-jwt-secret-key-32-chars-minimum!}")
    private String secretString;

    private Key signingKey;

    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7; // 7 days

    @PostConstruct
    private void initKey() {
        if (secretString == null || secretString.trim().length() < 32) {
            secretString = "default-companyos-dev-jwt-secret-key-32-chars-minimum!";
        }
        byte[] keyBytes = secretString.getBytes();
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a signed JWT token with multi-tenant context.
     */
    public String generateToken(Long userId, String username, String role, Long organizationId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("organizationId", organizationId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(signingKey)
                .compact();
    }

    public String generateToken(String username, String role) {
        return generateToken(null, username, role, null);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    public Long extractOrganizationId(String token) {
        Object orgId = parseClaims(token).get("organizationId");
        if (orgId instanceof Number) {
            return ((Number) orgId).longValue();
        }
        return null;
    }

    public Long extractUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
