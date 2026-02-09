package com.example.staffassign.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectPlan {
    private Long id;
    private Long projectId;

    @NotNull
    @Min(1)
    private Integer planVersion;

    @NotNull
    @Min(200001)
    @Max(209912)
    private Integer startYm;

    @NotNull
    @Min(200001)
    @Max(209912)
    private Integer endYm;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal resourceCount;

    @NotNull
    @Min(0)
    private Long productionAmount;

    private LocalDateTime createdAt;
}
