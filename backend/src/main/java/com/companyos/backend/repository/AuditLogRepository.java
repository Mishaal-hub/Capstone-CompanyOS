package com.companyos.backend.repository;

import com.companyos.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrganizationIdOrderByTimestampDesc(Long organizationId);
    List<AuditLog> findByOrganizationIdAndEntityTypeOrderByTimestampDesc(Long organizationId, String entityType);
}
