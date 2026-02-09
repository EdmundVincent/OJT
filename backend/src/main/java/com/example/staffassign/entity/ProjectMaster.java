package com.example.staffassign.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMaster {
    private Long id;
    private String name;
    private String description;
    private String customerId;
    private String endUser;
    private Integer startYm;
    private Integer endYm;
    private Long parentId;
    private Long revenue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
