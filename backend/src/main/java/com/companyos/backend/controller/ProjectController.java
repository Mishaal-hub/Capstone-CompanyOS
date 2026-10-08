package com.companyos.backend.controller;

import com.companyos.backend.dto.MilestoneDTO;
import com.companyos.backend.dto.ProjectRequestDTO;
import com.companyos.backend.dto.ProjectResponseDTO;
import com.companyos.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO request) {
        ProjectResponseDTO created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @RequestParam(defaultValue = "MEMBER") String role) {
        projectService.addMemberToProject(id, employeeId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{empId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long empId) {
        projectService.removeMemberFromProject(id, empId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/milestones")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MilestoneDTO> addMilestone(
            @PathVariable Long id,
            @RequestBody MilestoneDTO milestoneDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addMilestone(id, milestoneDTO));
    }

    @DeleteMapping("/{id}/milestones/{milestoneId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long id,
            @PathVariable Long milestoneId) {
        projectService.deleteMilestone(id, milestoneId);
        return ResponseEntity.noContent().build();
    }
}
