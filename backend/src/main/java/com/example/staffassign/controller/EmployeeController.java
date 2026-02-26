package com.example.staffassign.controller;

import com.example.staffassign.dto.EmployeeHistoryItem;
import com.example.staffassign.entity.EmployeeMaster;
import com.example.staffassign.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeMaster> getAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeMaster getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(@RequestBody EmployeeMaster employee) {
        employeeService.create(employee);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @RequestBody EmployeeMaster employee) {
        employeeService.update(id, employee);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @GetMapping("/{id}/history")
    public List<EmployeeHistoryItem> getHistory(@PathVariable Long id) {
        return employeeService.getHistory(id);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"employees.csv\"");
        
        // Write BOM for Excel
        response.getWriter().write('\ufeff');
        
        employeeService.exportCsv(response.getWriter());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importCsv(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            employeeService.importCsv(reader);
        }
    }
}
