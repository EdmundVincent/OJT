package com.example.staffassign.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class CodeMaster implements Serializable {
    private String categoryId;
    private String categoryName;
    private String codeId;
    private String codeName;
    private Integer sortOrder;
}
