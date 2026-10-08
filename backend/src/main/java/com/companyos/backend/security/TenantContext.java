package com.companyos.backend.security;

/**
 * TenantContext — ThreadLocal context holder for the current authenticated organization/tenant.
 *
 * Populated automatically by JwtAuthFilter on every authenticated request.
 * Services and Repositories use this to enforce multi-tenant data isolation.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static Long getRequiredTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No active tenant context found for the current request.");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
