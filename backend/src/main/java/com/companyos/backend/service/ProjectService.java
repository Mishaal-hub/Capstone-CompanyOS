package com.companyos.backend.service;

import com.companyos.backend.dto.MilestoneDTO;
import com.companyos.backend.dto.ProjectRequestDTO;
import com.companyos.backend.dto.ProjectResponseDTO;

import java.util.List;

public interface ProjectService {
    List<ProjectResponseDTO> getAllProjects();
    ProjectResponseDTO getProjectById(Long id);
    ProjectResponseDTO createProject(ProjectRequestDTO request);
    ProjectResponseDTO updateProject(Long id, ProjectRequestDTO request);
    void deleteProject(Long id);

    void addMemberToProject(Long projectId, Long employeeId, String role);
    void removeMemberFromProject(Long projectId, Long employeeId);

    MilestoneDTO addMilestone(Long projectId, MilestoneDTO milestoneDTO);
    void deleteMilestone(Long projectId, Long milestoneId);
}
