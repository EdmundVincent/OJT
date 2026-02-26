package com.example.staffassign.dto;

import lombok.Data;

@Data
public class EmployeeHistoryItem {
    private Long assignmentId;
    private Long projectId;
    private String projectName;
    private String projectStatus;
    private Integer startYm;
    private Integer endYm;
    private String role;
}

