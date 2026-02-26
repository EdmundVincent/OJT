package com.example.staffassign.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private String actionType;
    private String targetType;
    private String targetId;
    private String detail;
    private String performedBy;
    private LocalDateTime performedAt;
}

