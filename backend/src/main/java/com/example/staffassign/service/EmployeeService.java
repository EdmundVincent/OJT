package com.example.staffassign.service;

import com.example.staffassign.dto.EmployeeCsvDto;
import com.example.staffassign.entity.EmployeeMaster;
import com.example.staffassign.repository.EmployeeMasterMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeMasterMapper employeeMasterMapper;

    public EmployeeService(EmployeeMasterMapper employeeMasterMapper) {
        this.employeeMasterMapper = employeeMasterMapper;
    }

    public List<EmployeeMaster> findAll() {
        return employeeMasterMapper.findAll();
    }

    public EmployeeMaster findById(Long id) {
        return employeeMasterMapper.findById(id).orElse(null);
    }

    public void create(EmployeeMaster employee) {
        employeeMasterMapper.insert(employee);
    }

    public void update(Long id, EmployeeMaster employee) {
        employee.setId(id);
        employeeMasterMapper.update(employee);
    }

    public void delete(Long id) {
        employeeMasterMapper.delete(id);
    }

    @Transactional
    public void importCsv(Reader reader) {
        List<EmployeeCsvDto> dtos = new CsvToBeanBuilder<EmployeeCsvDto>(reader)
                .withType(EmployeeCsvDto.class)
                .build()
                .parse();

        for (EmployeeCsvDto dto : dtos) {
            EmployeeMaster entity = new EmployeeMaster();
            entity.setName(dto.getName());
            entity.setJoinYear(dto.getJoinYear());
            entity.setRank(dto.getRank());
            entity.setDepartment(dto.getDepartment());
            entity.setRemarks(dto.getRemarks());

            if (dto.getId() != null && employeeMasterMapper.findById(dto.getId()).isPresent()) {
                entity.setId(dto.getId());
                employeeMasterMapper.update(entity);
            } else {
                employeeMasterMapper.insert(entity);
            }
        }
    }

    public void exportCsv(Writer writer) throws Exception {
        List<EmployeeMaster> list = findAll();
        List<EmployeeCsvDto> dtos = list.stream().map(e -> {
            EmployeeCsvDto dto = new EmployeeCsvDto();
            dto.setId(e.getId());
            dto.setName(e.getName());
            dto.setJoinYear(e.getJoinYear());
            dto.setRank(e.getRank());
            dto.setDepartment(e.getDepartment());
            dto.setRemarks(e.getRemarks());
            return dto;
        }).collect(Collectors.toList());

        new StatefulBeanToCsvBuilder<EmployeeCsvDto>(writer)
                .build()
                .write(dtos);
    }
}
