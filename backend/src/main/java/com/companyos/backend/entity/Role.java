package com.companyos.backend.entity;

/**
 * Role — Role-based access control (RBAC) definitions for CompanyOS.
 */
public enum Role {
    SUPER_ADMIN,
    OWNER,
    MANAGER,
    EMPLOYEE,
    CLIENT;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return EMPLOYEE;
        try {
            return Role.valueOf(roleStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return EMPLOYEE;
        }
    }
}
