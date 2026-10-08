package com.companyos.backend.dto;

/**
 * Outgoing payload for a successful POST /api/auth/login.
 */
public class LoginResponse {

    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private Long organizationId;
    private String organizationName;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long userId, String username, String role, String token) {
        this.userId   = userId;
        this.username = username;
        this.role     = role;
        this.token    = token;
    }

    public LoginResponse(Long userId, String username, String fullName, String role, Long organizationId, String organizationName, String token) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
