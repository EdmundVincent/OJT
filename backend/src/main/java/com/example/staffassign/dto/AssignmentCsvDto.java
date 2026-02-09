package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AssignmentCsvDto {
    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "プロジェクトID", required = true)
    private Long projectId;

    @CsvBindByName(column = "社員ID", required = true)
    private Long employeeId;

    @CsvBindByName(column = "開始年月", required = true)
    private Integer startYm;

    @CsvBindByName(column = "終了年月", required = true)
    private Integer endYm;

    @CsvBindByName(column = "単価", required = true)
    private Integer unitPrice;

    @CsvBindByName(column = "割当比", required = true)
    private BigDecimal allocationRatio;
}
