package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.AuditLog;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(Users user, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logAction(Users user, String action, String details, String ipAddress) {
        logAction(user, action, null, null, details, ipAddress);
    }
}
