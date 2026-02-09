package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class EmployeeCsvDto {
    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "氏名", required = true)
    private String name;

    @CsvBindByName(column = "入社年", required = true)
    private Integer joinYear;

    @CsvBindByName(column = "ランク")
    private String rank;

    @CsvBindByName(column = "部門")
    private String department;

    @CsvBindByName(column = "備考")
    private String remarks;
}
