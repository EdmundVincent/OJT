package com.example.staffassign.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserMaster {
    private String email;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String role;
    
    @JsonIgnore
    private String passwordHash;
    
    private LocalDateTime createdAt;
}
