package com.example.staffassign.entity;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Assignment {
    private Long id;

    @NotNull
    private Long projectId;

    @NotNull
    private Long employeeId;

    @NotNull
    @Min(200001)
    @Max(209912)
    private Integer startYm;

    @NotNull
    @Min(200001)
    @Max(209912)
    private Integer endYm;

    @NotNull
    @Min(0)
    private Integer unitPrice;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("1.50")
    private BigDecimal allocationRatio;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
