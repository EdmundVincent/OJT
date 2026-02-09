package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CodeMasterCsvDto {
    @CsvBindByName(column = "種別ID", required = true)
    private String categoryId;

    @CsvBindByName(column = "種別名", required = true)
    private String categoryName;

    @CsvBindByName(column = "コードID", required = true)
    private String codeId;

    @CsvBindByName(column = "コード名", required = true)
    private String codeName;

    @CsvBindByName(column = "表示順")
    private Integer sortOrder;
}
