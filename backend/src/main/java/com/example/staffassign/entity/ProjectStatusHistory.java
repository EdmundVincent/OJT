package com.example.staffassign.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectStatusHistory {
    private Long id;
    private Long projectId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;
}

