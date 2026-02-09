package com.example.staffassign.controller;

import com.example.staffassign.entity.CodeMaster;
import com.example.staffassign.service.CodeMasterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code-master")
public class CodeMasterController {
    private final CodeMasterService codeMasterService;

    public CodeMasterController(CodeMasterService codeMasterService) {
        this.codeMasterService = codeMasterService;
    }

    @GetMapping
    public List<CodeMaster> getAll(@RequestParam(required = false) String categoryId) {
        if (categoryId != null) {
            return codeMasterService.findByCategory(categoryId);
        }
        return codeMasterService.findAll();
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            codeMasterService.importCsv(reader);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportCsv(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"code_master.csv\"");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            codeMasterService.exportCsv(writer);
        }
    }
}
