package com.example.staffassign.service;

import com.example.staffassign.entity.AuditLog;
import com.example.staffassign.repository.AuditLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void log(String actionType, String targetType, String targetId, String detail) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(actionType);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setDetail(detail);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                log.setPerformedBy(auth.getName());
            }
            log.setPerformedAt(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception ignore) {
            // ignore audit failures to avoid blocking main business flow
        }
    }
}
