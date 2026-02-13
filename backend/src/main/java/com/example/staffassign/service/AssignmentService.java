package com.example.staffassign.service;

import com.example.staffassign.entity.Assignment;
import com.example.staffassign.exception.BusinessException;
import com.example.staffassign.repository.AssignmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.staffassign.dto.AssignmentCsvDto;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentMapper assignmentMapper;

    public AssignmentService(AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    public List<Assignment> findAll() {
        return assignmentMapper.findAll();
    }

    public List<Assignment> findByProjectId(Long projectId) {
        return assignmentMapper.findByProjectId(projectId);
    }
    
    public List<Assignment> findByEmployeeId(Long employeeId) {
        return assignmentMapper.findByEmployeeId(employeeId);
    }

    @Transactional
    public void importCsv(Reader reader) {
        List<AssignmentCsvDto> dtos = new CsvToBeanBuilder<AssignmentCsvDto>(reader)
                .withType(AssignmentCsvDto.class).build().parse();
        
        for (AssignmentCsvDto dto : dtos) {
            Assignment entity = new Assignment();
            entity.setProjectId(dto.getProjectId());
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setStartYm(dto.getStartYm());
            entity.setEndYm(dto.getEndYm());
            entity.setUnitPrice(dto.getUnitPrice());
            entity.setAllocationRatio(dto.getAllocationRatio());
            
            // 連続性チェック: 同一プロジェクト・同一要員の既存アサインの終了年月より後であることを確認
            Integer maxEndYm = assignmentMapper.findMaxEndYm(dto.getProjectId(), dto.getEmployeeId());
            if (maxEndYm != null) {
                // 開始年月が以前の終了年月以下であればエラー（重複または逆転）
                if (dto.getStartYm() <= maxEndYm) {
                     throw new BusinessException("Import Error: Project ID " + dto.getProjectId() + 
                             ", Employee ID " + dto.getEmployeeId() +
                             " - Start YM (" + dto.getStartYm() + ") must be greater than previous End YM (" + maxEndYm + ")");
                }
            }

            if (dto.getId() != null && assignmentMapper.findById(dto.getId()).isPresent()) {
                entity.setId(dto.getId());
                assignmentMapper.update(entity);
            } else {
                assignmentMapper.insert(entity);
            }
        }
    }

    public void exportCsv(Writer writer) {
        try {
            List<Assignment> list = assignmentMapper.findAll();
            List<AssignmentCsvDto> dtos = list.stream().map(e -> {
                AssignmentCsvDto dto = new AssignmentCsvDto();
                dto.setId(e.getId());
                dto.setProjectId(e.getProjectId());
                dto.setEmployeeId(e.getEmployeeId());
                dto.setStartYm(e.getStartYm());
                dto.setEndYm(e.getEndYm());
                dto.setUnitPrice(e.getUnitPrice());
                dto.setAllocationRatio(e.getAllocationRatio());
                return dto;
            }).toList();

            StatefulBeanToCsv<AssignmentCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<AssignmentCsvDto>(writer).build();
            beanToCsv.write(dtos);
        } catch (Exception e) {
            throw new BusinessException("Failed to export CSV: " + e.getMessage());
        }
    }

    @Transactional
    public void create(Assignment assignment) {
        Integer maxEndYm = assignmentMapper.findMaxEndYm(assignment.getProjectId(), assignment.getEmployeeId());
        if (maxEndYm != null) {
            if (assignment.getStartYm() <= maxEndYm) {
                throw new BusinessException("Start YM must be greater than previous End YM (" + maxEndYm + ")");
            }
        }
        assignmentMapper.insert(assignment);
    }

    public void update(Long id, Assignment assignment) {
        assignment.setId(id);
        assignmentMapper.update(assignment);
    }

    public void delete(Long id) {
        assignmentMapper.delete(id);
    }
}
