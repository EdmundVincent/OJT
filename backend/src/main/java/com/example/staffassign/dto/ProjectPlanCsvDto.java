package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectPlanCsvDto {
    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "プロジェクトID", required = true)
    private Long projectId;

    @CsvBindByName(column = "計画版数", required = true)
    private Integer planVersion;

    @CsvBindByName(column = "開始年月", required = true)
    private Integer startYm;

    @CsvBindByName(column = "終了年月", required = true)
    private Integer endYm;

    @CsvBindByName(column = "要員数", required = true)
    private BigDecimal resourceCount;

    @CsvBindByName(column = "生産額", required = true)
    private Long productionAmount;
}
