package com.example.staffassign.service;

import com.example.staffassign.entity.CodeMaster;
import com.example.staffassign.repository.CodeMasterMapper;
import org.springframework.stereotype.Service;
import com.example.staffassign.dto.CodeMasterCsvDto;
import com.example.staffassign.exception.BusinessException;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

@Service
public class CodeMasterService {
    private final CodeMasterMapper codeMasterMapper;

    public CodeMasterService(CodeMasterMapper codeMasterMapper) {
        this.codeMasterMapper = codeMasterMapper;
    }

    public List<CodeMaster> findAll() {
        return codeMasterMapper.findAll();
    }

    public List<CodeMaster> findByCategory(String categoryId) {
        return codeMasterMapper.findByCategory(categoryId);
    }

    @Transactional
    public void importCsv(Reader reader) {
        List<CodeMasterCsvDto> dtos = new CsvToBeanBuilder<CodeMasterCsvDto>(reader)
                .withType(CodeMasterCsvDto.class).build().parse();
        
        for (CodeMasterCsvDto dto : dtos) {
            CodeMaster entity = new CodeMaster();
            entity.setCategoryId(dto.getCategoryId());
            entity.setCategoryName(dto.getCategoryName());
            entity.setCodeId(dto.getCodeId());
            entity.setCodeName(dto.getCodeName());
            entity.setSortOrder(dto.getSortOrder());
            
            if (codeMasterMapper.findByKey(dto.getCategoryId(), dto.getCodeId()) != null) {
                codeMasterMapper.update(entity);
            } else {
                codeMasterMapper.insert(entity);
            }
        }
    }

    public void exportCsv(Writer writer) {
        try {
            List<CodeMaster> list = codeMasterMapper.findAll();
            List<CodeMasterCsvDto> dtos = list.stream().map(e -> {
                CodeMasterCsvDto dto = new CodeMasterCsvDto();
                dto.setCategoryId(e.getCategoryId());
                dto.setCategoryName(e.getCategoryName());
                dto.setCodeId(e.getCodeId());
                dto.setCodeName(e.getCodeName());
                dto.setSortOrder(e.getSortOrder());
                return dto;
            }).toList();

            StatefulBeanToCsv<CodeMasterCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<CodeMasterCsvDto>(writer).build();
            beanToCsv.write(dtos);
        } catch (Exception e) {
            throw new BusinessException("Failed to export CSV: " + e.getMessage());
        }
    }
}
