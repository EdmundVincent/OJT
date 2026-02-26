package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ProjectCsvDto {
    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "名称", required = true)
    private String name;

    @CsvBindByName(column = "概要")
    private String description;

    @CsvBindByName(column = "お客様ID")
    private String customerId;

    @CsvBindByName(column = "エンドユーザ")
    private String endUser;

    @CsvBindByName(column = "開始年月", required = true)
    private Integer startYm;

    @CsvBindByName(column = "終了年月")
    private Integer endYm;

    @CsvBindByName(column = "親ID")
    private Long parentId;

    @CsvBindByName(column = "売上")
    private Long revenue;

    @CsvBindByName(column = "PM ID")
    private Long pmId;

    @CsvBindByName(column = "優先度")
    private String priority;

    @CsvBindByName(column = "プロジェクト種別")
    private String projectType;

    @CsvBindByName(column = "ステータス")
    private String status;
}
