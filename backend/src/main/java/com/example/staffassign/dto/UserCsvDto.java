package com.example.staffassign.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserCsvDto {
    @CsvBindByName(column = "Email", required = true)
    private String email;

    @CsvBindByName(column = "Role", required = true)
    private String role;

    @CsvBindByName(column = "Valid From", required = true)
    @CsvDate("yyyy-MM-dd")
    private LocalDate validFrom;

    @CsvBindByName(column = "Valid To")
    @CsvDate("yyyy-MM-dd")
    private LocalDate validTo;

    @CsvBindByName(column = "Password")
    private String password;
}
