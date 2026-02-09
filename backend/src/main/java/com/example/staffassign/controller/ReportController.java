package com.example.staffassign.controller;

import com.example.staffassign.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/resource-allocation")
    public List<Map<String, Object>> getResourceAllocation(@RequestBody Map<String, Object> params) {
        return reportService.getResourceAllocation(params);
    }

    @PostMapping("/budget-mismatch")
    public List<Map<String, Object>> getBudgetMismatch(@RequestBody Map<String, Object> params) {
        return reportService.getBudgetMismatch(params);
    }

    @PostMapping("/project-cost")
    public List<Map<String, Object>> getProjectCost(@RequestBody Map<String, Object> params) {
        return reportService.getProjectCost(params);
    }

    @PostMapping("/resource-allocation/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportResourceAllocation(@RequestBody Map<String, Object> params, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"resource_allocation.csv\"");
        List<Map<String, Object>> data = reportService.getResourceAllocation(params);
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            reportService.exportCsv(data, writer);
        }
    }

    @PostMapping("/budget-mismatch/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportBudgetMismatch(@RequestBody Map<String, Object> params, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"budget_mismatch.csv\"");
        List<Map<String, Object>> data = reportService.getBudgetMismatch(params);
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            reportService.exportCsv(data, writer);
        }
    }

    @PostMapping("/project-cost/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportProjectCost(@RequestBody Map<String, Object> params, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"project_cost.csv\"");
        List<Map<String, Object>> data = reportService.getProjectCost(params);
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            reportService.exportCsv(data, writer);
        }
    }
}
