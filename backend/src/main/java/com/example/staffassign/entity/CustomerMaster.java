package com.example.staffassign.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerMaster {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
