package com.companyos.backend.service;

import com.companyos.backend.entity.AuditLog;
import java.util.List;

public interface AuditService {
    void log(String action, String entityType, Long entityId, String description);
    List<AuditLog> getRecentLogs();
}
