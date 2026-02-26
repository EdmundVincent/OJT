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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentMapper assignmentMapper;
    private final AuditLogService auditLogService;

    public AssignmentService(AssignmentMapper assignmentMapper, AuditLogService auditLogService) {
        this.assignmentMapper = assignmentMapper;
        this.auditLogService = auditLogService;
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

    private void checkUtilizationConflict(Assignment assignment) {
        List<Assignment> existingAssignments = assignmentMapper.findByEmployeeId(assignment.getEmployeeId());
        
        int startYm = assignment.getStartYm();
        int endYm = assignment.getEndYm();
        
        for (int ym = startYm; ym <= endYm; ) {
            BigDecimal totalRatio = assignment.getAllocationRatio();
            for (Assignment existing : existingAssignments) {
                // Skip the current assignment being updated
                if (assignment.getId() != null && existing.getId().equals(assignment.getId())) {
                    continue;
                }
                if (ym >= existing.getStartYm() && ym <= existing.getEndYm()) {
                    totalRatio = totalRatio.add(existing.getAllocationRatio());
                }
            }
            if (totalRatio.compareTo(new BigDecimal("1.00")) > 0) {
                throw new BusinessException(String.format("%d月の合計稼働率が%.2fとなり、1.0を超えています。", ym, totalRatio));
            }
            
            // Advance ym
            int year = ym / 100;
            int month = ym % 100;
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
            ym = year * 100 + month;
        }
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
            
            checkUtilizationConflict(entity);

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
            throw new BusinessException("CSVエクスポートに失敗しました: " + e.getMessage());
        }
    }

    @Transactional
    public void create(Assignment assignment) {
        checkUtilizationConflict(assignment);
        assignmentMapper.insert(assignment);
        if (assignment.getId() != null) {
            auditLogService.log("ASSIGNMENT_CREATE", "ASSIGNMENT", String.valueOf(assignment.getId()), String.valueOf(assignment.getEmployeeId()));
        }
    }

    public void update(Long id, Assignment assignment) {
        assignment.setId(id);
        checkUtilizationConflict(assignment);
        assignmentMapper.update(assignment);
        auditLogService.log("ASSIGNMENT_UPDATE", "ASSIGNMENT", String.valueOf(id), String.valueOf(assignment.getEmployeeId()));
    }

    public void delete(Long id) {
        assignmentMapper.delete(id);
        auditLogService.log("ASSIGNMENT_DELETE", "ASSIGNMENT", String.valueOf(id), null);
    }
}
