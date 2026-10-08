package com.companyos.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a CompanyOS user account.
 *
 * Maps to the `users` table. Hibernate (ddl-auto=update) adds new columns
 * automatically on startup — no manual migration needed for new fields.
 *
 * username     — the user's email address (unique, used as login identifier)
 * password     — BCrypt-hashed; nullable for pure Google-OAuth accounts
 * role         — OWNER | ADMIN | MEMBER (enforced by AuthService)
 * status       — ACTIVE | INACTIVE | LOCKED
 * verified     — true when email is confirmed (always true for Google-OAuth users)
 * authProvider — LOCAL | GOOGLE | LOCAL,GOOGLE  (tracks which auth methods are linked)
 * googleId     — Google subject ID ("sub" claim from verified ID token)
 * avatarUrl    — Google profile picture URL (optional, display-only)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    /**
     * BCrypt-hashed password. Nullable: Google-only accounts have no password.
     * NEVER return this field in any API response.
     */
    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "full_name")
    private String fullName;

    /**
     * Foreign key to the Organization table (tenant identifier).
     * Set during registration/Google sign-in when workspace is created.
     */
    @Column(name = "organization_id")
    private Long organizationId;

    /**
     * Soft link to the Employee record for this user, if one was created.
     * Used by DashboardService to scope tasks.
     */
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * Account lifecycle status.
     * ACTIVE   — normal, can log in once verified
     * INACTIVE — disabled by an administrator
     * LOCKED   — locked after too many failed attempts (future feature)
     */
    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    // ── Google OAuth fields ──────────────────────────────────────────────────

    /**
     * Stable Google subject ID ("sub" claim from Google ID token).
     * Unique per Google account. Used as the primary Google identity key.
     * Null for accounts created via email/password only.
     */
    @Column(name = "google_id", unique = true)
    private String googleId;

    /**
     * Which authentication providers are linked to this account.
     * Values: "LOCAL", "GOOGLE", "LOCAL,GOOGLE"
     * Defaults to "LOCAL" for backward compatibility.
     */
    @Column(name = "auth_provider", nullable = false)
    private String authProvider = "LOCAL";

    /**
     * Google profile picture URL (optional). Never used for authentication —
     * only for display purposes. May be stale between logins.
     */
    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    // ── Audit timestamps ────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Set createdAt automatically before first INSERT.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    // ── userId ──────────────────────────────────────────────────────────────
    public Long getUserId()              { return userId; }
    public void setUserId(Long userId)   { this.userId = userId; }

    // ── username / email ─────────────────────────────────────────────────────
    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    // ── password (always hashed) ─────────────────────────────────────────────
    public String getPassword()                  { return password; }
    public void setPassword(String password)     { this.password = password; }

    // ── role ─────────────────────────────────────────────────────────────────
    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    // ── fullName ─────────────────────────────────────────────────────────────
    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    // ── organizationId ───────────────────────────────────────────────────────
    public Long getOrganizationId()                      { return organizationId; }
    public void setOrganizationId(Long organizationId)  { this.organizationId = organizationId; }

    // ── employeeId ───────────────────────────────────────────────────────────
    public Long getEmployeeId()                  { return employeeId; }
    public void setEmployeeId(Long employeeId)   { this.employeeId = employeeId; }

    // ── status ───────────────────────────────────────────────────────────────
    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    // ── verified ─────────────────────────────────────────────────────────────
    public boolean isVerified()                  { return verified; }
    public void setVerified(boolean verified)    { this.verified = verified; }

    // ── verificationCode ─────────────────────────────────────────────────────
    public String getVerificationCode()                          { return verificationCode; }
    public void setVerificationCode(String verificationCode)    { this.verificationCode = verificationCode; }

    // ── Google OAuth ─────────────────────────────────────────────────────────
    public String getGoogleId()                  { return googleId; }
    public void setGoogleId(String googleId)     { this.googleId = googleId; }

    public String getAuthProvider()                      { return authProvider; }
    public void setAuthProvider(String authProvider)     { this.authProvider = authProvider; }

    public String getAvatarUrl()                 { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }

    // ── createdAt ─────────────────────────────────────────────────────────────
    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }

    // ── lastLogin ─────────────────────────────────────────────────────────────
    public LocalDateTime getLastLogin()                  { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin)    { this.lastLogin = lastLogin; }
}
