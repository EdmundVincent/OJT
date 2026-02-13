package com.example.staffassign.controller;

import com.example.staffassign.entity.Assignment;
import com.example.staffassign.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<Assignment> getAssignments(@RequestParam(required = false) Long projectId, 
                                           @RequestParam(required = false) Long employeeId) {
        if (projectId != null) {
            return assignmentService.findByProjectId(projectId);
        }
        if (employeeId != null) {
            return assignmentService.findByEmployeeId(employeeId);
        }
        return assignmentService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(@RequestBody @Valid Assignment assignment) {
        assignmentService.create(assignment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @RequestBody @Valid Assignment assignment) {
        assignmentService.update(id, assignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            assignmentService.importCsv(reader);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportCsv(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"assignments.csv\"");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            assignmentService.exportCsv(writer);
        }
    }
}
