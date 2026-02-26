package com.example.staffassign.controller;

import com.example.staffassign.entity.ProjectMaster;
import com.example.staffassign.entity.ProjectPlan;
import com.example.staffassign.entity.ProjectStatusHistory;
import com.example.staffassign.service.ProjectService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectMaster> getAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectMaster getById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @GetMapping("/{id}/status-history")
    public List<ProjectStatusHistory> getStatusHistory(@PathVariable Long id) {
        return projectService.findStatusHistory(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(@RequestBody ProjectMaster project) {
        projectService.create(project);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @RequestBody ProjectMaster project) {
        projectService.update(id, project);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    // Plans
    @GetMapping("/{id}/plans")
    public List<ProjectPlan> getPlans(@PathVariable Long id) {
        return projectService.findPlansByProjectId(id);
    }

    @PostMapping("/{id}/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public void createPlan(@PathVariable Long id, @RequestBody @Valid ProjectPlan plan) {
        plan.setProjectId(id);
        projectService.createPlan(plan);
    }
    
    @DeleteMapping("/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePlan(@PathVariable Long planId) {
        projectService.deletePlan(planId);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            projectService.importCsv(reader);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportCsv(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"projects.csv\"");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            projectService.exportCsv(writer);
        }
    }

    @PostMapping("/plans/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importPlanCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            projectService.importPlanCsv(reader);
        }
    }

    @GetMapping("/plans/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportPlanCsv(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"project_plans.csv\"");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            projectService.exportPlanCsv(writer);
        }
    }
}
