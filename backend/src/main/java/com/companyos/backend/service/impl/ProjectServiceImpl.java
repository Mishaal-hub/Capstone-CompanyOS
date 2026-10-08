package com.companyos.backend.service.impl;

import com.companyos.backend.dto.EmployeeResponseDTO;
import com.companyos.backend.dto.MilestoneDTO;
import com.companyos.backend.dto.ProjectRequestDTO;
import com.companyos.backend.dto.ProjectResponseDTO;
import com.companyos.backend.entity.Milestone;
import com.companyos.backend.entity.Project;
import com.companyos.backend.entity.ProjectMember;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.EmployeeRepository;
import com.companyos.backend.repository.MilestoneRepository;
import com.companyos.backend.repository.ProjectMemberRepository;
import com.companyos.backend.repository.ProjectRepository;
import com.companyos.backend.repository.TaskRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.EmployeeService;
import com.companyos.backend.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository memberRepository,
            MilestoneRepository milestoneRepository,
            TaskRepository taskRepository,
            EmployeeRepository employeeRepository,
            EmployeeService employeeService) {

        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {
        Long orgId = TenantContext.getRequiredTenantId();
        return projectRepository.findByOrganizationId(orgId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Project project = projectRepository.findByProjectIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapToDTO(project);
    }

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO request) {
        Long orgId = TenantContext.getRequiredTenantId();

        Project project = new Project();
        project.setOrganizationId(orgId);
        project.setProjectName(request.getProjectName().trim());
        project.setDescription(request.getDescription());
        project.setClientId(request.getClientId());
        project.setProjectManagerId(request.getProjectManagerId());
        project.setBudget(request.getBudget());
        project.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        project.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNED");
        project.setProgress(request.getProgress() != null ? request.getProgress() : 0);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project saved = projectRepository.save(project);

        if (request.getMemberEmployeeIds() != null) {
            for (Long empId : request.getMemberEmployeeIds()) {
                memberRepository.save(new ProjectMember(saved.getProjectId(), empId, "MEMBER"));
            }
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO request) {
        Long orgId = TenantContext.getRequiredTenantId();
        Project project = projectRepository.findByProjectIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setProjectName(request.getProjectName().trim());
        project.setDescription(request.getDescription());
        project.setClientId(request.getClientId());
        project.setProjectManagerId(request.getProjectManagerId());
        project.setBudget(request.getBudget());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getProgress() != null) project.setProgress(request.getProgress());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project updated = projectRepository.save(project);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Project project = projectRepository.findByProjectIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public void addMemberToProject(Long projectId, Long employeeId, String role) {
        Long orgId = TenantContext.getRequiredTenantId();
        projectRepository.findByProjectIdAndOrganizationId(projectId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (memberRepository.findByProjectIdAndEmployeeId(projectId, employeeId).isEmpty()) {
            memberRepository.save(new ProjectMember(projectId, employeeId, role));
        }
    }

    @Override
    @Transactional
    public void removeMemberFromProject(Long projectId, Long employeeId) {
        Long orgId = TenantContext.getRequiredTenantId();
        projectRepository.findByProjectIdAndOrganizationId(projectId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        memberRepository.deleteByProjectIdAndEmployeeId(projectId, employeeId);
    }

    @Override
    @Transactional
    public MilestoneDTO addMilestone(Long projectId, MilestoneDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        projectRepository.findByProjectIdAndOrganizationId(projectId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Milestone milestone = new Milestone();
        milestone.setProjectId(projectId);
        milestone.setOrganizationId(orgId);
        milestone.setTitle(dto.getTitle());
        milestone.setDescription(dto.getDescription());
        milestone.setDueDate(dto.getDueDate());
        milestone.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        Milestone saved = milestoneRepository.save(milestone);
        dto.setMilestoneId(saved.getMilestoneId());
        dto.setProjectId(saved.getProjectId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteMilestone(Long projectId, Long milestoneId) {
        Long orgId = TenantContext.getRequiredTenantId();
        Milestone milestone = milestoneRepository.findByMilestoneIdAndOrganizationId(milestoneId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));
        milestoneRepository.delete(milestone);
    }

    private ProjectResponseDTO mapToDTO(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setProjectId(project.getProjectId());
        dto.setOrganizationId(project.getOrganizationId());
        dto.setProjectName(project.getProjectName());
        dto.setDescription(project.getDescription());
        dto.setClientId(project.getClientId());
        dto.setProjectManagerId(project.getProjectManagerId());

        if (project.getProjectManagerId() != null) {
            employeeRepository.findById(project.getProjectManagerId()).ifPresent(emp -> {
                dto.setProjectManagerName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            });
        }

        dto.setBudget(project.getBudget());
        dto.setPriority(project.getPriority());
        dto.setStatus(project.getStatus());
        dto.setProgress(project.getProgress());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Milestones
        List<MilestoneDTO> milestones = milestoneRepository.findByProjectId(project.getProjectId())
                .stream().map(m -> {
                    MilestoneDTO mdto = new MilestoneDTO();
                    mdto.setMilestoneId(m.getMilestoneId());
                    mdto.setProjectId(m.getProjectId());
                    mdto.setTitle(m.getTitle());
                    mdto.setDescription(m.getDescription());
                    mdto.setDueDate(m.getDueDate());
                    mdto.setStatus(m.getStatus());
                    return mdto;
                }).collect(Collectors.toList());
        dto.setMilestones(milestones);

        // Members
        List<ProjectMember> members = memberRepository.findByProjectId(project.getProjectId());
        List<EmployeeResponseDTO> memberDTOs = new ArrayList<>();
        for (ProjectMember pm : members) {
            employeeRepository.findById(pm.getEmployeeId()).ifPresent(emp ->
                memberDTOs.add(EmployeeResponseDTO.from(emp)));
        }
        dto.setMembers(memberDTOs);

        // Task statistics
        var tasks = taskRepository.findByProjectId(project.getProjectId());
        dto.setTotalTasks(tasks.size());
        dto.setCompletedTasks((int) tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        dto.setBlockedTasks((int) tasks.stream().filter(t -> "BLOCKED".equals(t.getStatus())).count());

        return dto;
    }
}
