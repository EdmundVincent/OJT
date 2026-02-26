package com.example.staffassign.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeMaster {
    private Long id;
    private String employeeCode;
    private String name;
    private Integer joinYear;
    private String rank;
    private String department;
    private String skills;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
