package com.example.staffassign.controller;

import com.example.staffassign.entity.UserMaster;
import com.example.staffassign.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserMaster> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserMaster getByEmail(@PathVariable String email) {
        Optional<UserMaster> user = userService.findByEmail(email);
        return user.orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(@RequestBody UserCreateRequest req) {
        UserMaster user = new UserMaster();
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setValidFrom(req.getValidFrom() != null ? req.getValidFrom() : LocalDate.now());
        user.setValidTo(req.getValidTo());
        userService.create(user, req.getPassword());
    }

    @PutMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable String email, @RequestBody UserUpdateRequest req) {
        UserMaster user = new UserMaster();
        user.setEmail(email);
        user.setRole(req.getRole());
        user.setValidFrom(req.getValidFrom());
        user.setValidTo(req.getValidTo());
        userService.update(user, req.getPassword());
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String email) {
        userService.delete(email);
    }

    @Data
    public static class UserCreateRequest {
        private String email;
        private String role; // ADMIN/USER
        private LocalDate validFrom;
        private LocalDate validTo;
        private String password;
    }

    @Data
    public static class UserUpdateRequest {
        private String role;
        private LocalDate validFrom;
        private LocalDate validTo;
        private String password; // optional; if provided, reset password
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public void importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            userService.importCsv(reader);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportCsv(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"users.csv\"");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF"); // BOM
            userService.exportCsv(writer);
        }
    }
}
