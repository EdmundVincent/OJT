package com.example.staffassign.service;

import com.example.staffassign.dto.EmployeeCsvDto;
import com.example.staffassign.dto.EmployeeHistoryItem;
import com.example.staffassign.entity.EmployeeMaster;
import com.example.staffassign.exception.BusinessException;
import com.example.staffassign.repository.EmployeeHistoryMapper;
import com.example.staffassign.repository.EmployeeMasterMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeMasterMapper employeeMasterMapper;
    private final EmployeeHistoryMapper employeeHistoryMapper;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeMasterMapper employeeMasterMapper, EmployeeHistoryMapper employeeHistoryMapper, AuditLogService auditLogService) {
        this.employeeMasterMapper = employeeMasterMapper;
        this.employeeHistoryMapper = employeeHistoryMapper;
        this.auditLogService = auditLogService;
    }

    public List<EmployeeMaster> findAll() {
        return employeeMasterMapper.findAll();
    }

    public EmployeeMaster findById(Long id) {
        return employeeMasterMapper.findById(id).orElse(null);
    }

    private void validateEmployeeCode(EmployeeMaster employee) {
        String code = employee.getEmployeeCode();
        if (code == null || !code.matches("\\d{4}")) {
            throw new BusinessException("社員IDは4桁の数字で入力してください。");
        }
        
        Optional<EmployeeMaster> existing = employeeMasterMapper.findByEmployeeCode(code);
        if (existing.isPresent() && !existing.get().getId().equals(employee.getId())) {
            throw new BusinessException("この社員IDは既に使用されています。");
        }
    }

    public void create(EmployeeMaster employee) {
        validateEmployeeCode(employee);
        employeeMasterMapper.insert(employee);
        if (employee.getId() != null) {
            auditLogService.log("EMPLOYEE_CREATE", "EMPLOYEE", String.valueOf(employee.getId()), employee.getEmployeeCode());
        }
    }

    public void update(Long id, EmployeeMaster employee) {
        employee.setId(id);
        validateEmployeeCode(employee);
        employeeMasterMapper.update(employee);
        auditLogService.log("EMPLOYEE_UPDATE", "EMPLOYEE", String.valueOf(id), employee.getEmployeeCode());
    }

    public void delete(Long id) {
        employeeMasterMapper.delete(id);
        auditLogService.log("EMPLOYEE_DELETE", "EMPLOYEE", String.valueOf(id), null);
    }

    public List<EmployeeHistoryItem> getHistory(Long employeeId) {
        return employeeHistoryMapper.findByEmployeeId(employeeId);
    }

    @Transactional
    public void importCsv(Reader reader) {
        List<EmployeeCsvDto> dtos = new CsvToBeanBuilder<EmployeeCsvDto>(reader)
                .withType(EmployeeCsvDto.class)
                .build()
                .parse();

        for (EmployeeCsvDto dto : dtos) {
            EmployeeMaster entity = new EmployeeMaster();
            entity.setEmployeeCode(dto.getEmployeeCode());
            entity.setName(dto.getName());
            entity.setJoinYear(dto.getJoinYear());
            entity.setRank(dto.getRank());
            entity.setDepartment(dto.getDepartment());
            entity.setSkills(dto.getSkills());
            entity.setRemarks(dto.getRemarks());

            if (dto.getId() != null && employeeMasterMapper.findById(dto.getId()).isPresent()) {
                entity.setId(dto.getId());
                validateEmployeeCode(entity);
                employeeMasterMapper.update(entity);
            } else {
                validateEmployeeCode(entity);
                employeeMasterMapper.insert(entity);
            }
        }
    }

    public void exportCsv(Writer writer) throws Exception {
        List<EmployeeMaster> list = findAll();
        List<EmployeeCsvDto> dtos = list.stream().map(e -> {
            EmployeeCsvDto dto = new EmployeeCsvDto();
            dto.setId(e.getId());
            dto.setEmployeeCode(e.getEmployeeCode());
            dto.setName(e.getName());
            dto.setJoinYear(e.getJoinYear());
            dto.setRank(e.getRank());
            dto.setDepartment(e.getDepartment());
            dto.setSkills(e.getSkills());
            dto.setRemarks(e.getRemarks());
            return dto;
        }).collect(Collectors.toList());

        new StatefulBeanToCsvBuilder<EmployeeCsvDto>(writer)
                .build()
                .write(dtos);
    }
}
