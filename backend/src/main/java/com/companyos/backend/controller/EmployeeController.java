package com.companyos.backend.controller;

import com.companyos.backend.dto.EmployeeRequestDTO;
import com.companyos.backend.dto.EmployeeResponseDTO;
import com.companyos.backend.entity.Employee;
import com.companyos.backend.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EmployeeController — REST endpoints for employee management.
 *
 * All operations are scoped to the authenticated user's organization via TenantContext.
 * No employee data from other organizations is accessible.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO request) {
        Employee employee = request.toEntity();
        Employee saved = service.saveEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponseDTO.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = service.getAllEmployees()
                .stream()
                .map(EmployeeResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(EmployeeResponseDTO.from(service.getEmployee(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequestDTO request) {
        Employee updated = service.updateEmployee(id, request.toEntity());
        return ResponseEntity.ok(EmployeeResponseDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
