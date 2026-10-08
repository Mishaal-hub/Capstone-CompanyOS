package com.companyos.backend.service.impl;

import com.companyos.backend.entity.AuditLog;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.AuditLogRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void log(String action, String entityType, Long entityId, String description) {
        Long orgId = TenantContext.getTenantId();
        if (orgId == null) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth != null ? auth.getName() : "System";
        Long userId = null;

        if (auth != null && auth.getName() != null) {
            userId = userRepository.findByUsername(auth.getName()).map(User::getUserId).orElse(null);
        }

        AuditLog auditLog = new AuditLog(orgId, userId, userEmail, action, entityType, entityId, description);
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        Long orgId = TenantContext.getRequiredTenantId();
        return auditLogRepository.findTop50ByOrganizationIdOrderByTimestampDesc(orgId);
    }
}
