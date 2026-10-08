package com.companyos.backend.service.impl;

import com.companyos.backend.dto.DashboardOverviewDTO;
import com.companyos.backend.entity.Deal;
import com.companyos.backend.entity.Goal;
import com.companyos.backend.entity.Project;
import com.companyos.backend.entity.Task;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.*;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.AuditService;
import com.companyos.backend.service.DashboardService;
import com.companyos.backend.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final DealRepository dealRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final TaskService taskService;

    public DashboardServiceImpl(
            EmployeeRepository employeeRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            ClientRepository clientRepository,
            DealRepository dealRepository,
            GoalRepository goalRepository,
            UserRepository userRepository,
            AuditService auditService,
            TaskService taskService) {

        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.clientRepository = clientRepository;
        this.dealRepository = dealRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.taskService = taskService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewDTO getDashboardOverview() {
        Long orgId = TenantContext.getRequiredTenantId();

        DashboardOverviewDTO dto = new DashboardOverviewDTO();

        long employees = employeeRepository.count();
        long totalProjects = projectRepository.countByOrganizationId(orgId);
        List<Project> projects = projectRepository.findByOrganizationId(orgId);
        long activeProjects = projects.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus()) || "PLANNED".equals(p.getStatus())).count();

        List<Task> tasks = taskRepository.findByOrganizationId(orgId);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long pendingTasks = totalTasks - completedTasks;
        long overdueTasks = tasks.stream().filter(t -> t.getDueDate() != null && !"COMPLETED".equals(t.getStatus()) && t.getDueDate().isBefore(LocalDate.now())).count();
        long blockedTasks = tasks.stream().filter(t -> "BLOCKED".equals(t.getStatus())).count();

        long totalClients = clientRepository.countByOrganizationId(orgId);
        List<Deal> deals = dealRepository.findByOrganizationId(orgId);
        double pipelineValue = deals.stream().filter(d -> !"LOST".equals(d.getStage())).mapToDouble(Deal::getValue).sum();

        dto.setTotalEmployees(employees);
        dto.setTotalProjects(totalProjects);
        dto.setActiveProjects(activeProjects);
        dto.setTotalTasks(totalTasks);
        dto.setPendingTasks(pendingTasks);
        dto.setCompletedTasks(completedTasks);
        dto.setOverdueTasks(overdueTasks);
        dto.setBlockedTasks(blockedTasks);
        dto.setTotalClients(totalClients);
        dto.setTotalPipelineValue(pipelineValue);

        // Calculate Company Health Score
        int taskHealth = 100;
        if (totalTasks > 0) {
            int penalties = (int) (overdueTasks * 15 + blockedTasks * 20);
            taskHealth = Math.max(10, 100 - penalties);
        }

        int projectHealth = 100;
        if (!projects.isEmpty()) {
            double avgProgress = projects.stream().mapToInt(Project::getProgress).average().orElse(100.0);
            long delayedProjects = projects.stream().filter(p -> p.getEndDate() != null && !"COMPLETED".equals(p.getStatus()) && p.getEndDate().isBefore(LocalDate.now())).count();
            projectHealth = Math.max(10, Math.min(100, (int) Math.round(avgProgress - (delayedProjects * 15))));
        }

        List<Goal> goals = goalRepository.findByOrganizationId(orgId);
        int goalHealth = 100;
        if (!goals.isEmpty()) {
            goalHealth = (int) Math.round(goals.stream().mapToInt(Goal::getProgress).average().orElse(100.0));
        }

        int overallHealth = (int) Math.round((0.4 * taskHealth) + (0.4 * projectHealth) + (0.2 * goalHealth));
        overallHealth = Math.max(15, Math.min(100, overallHealth));

        dto.setCompanyHealthScore(overallHealth);
        dto.setHealthBreakdown(new DashboardOverviewDTO.HealthBreakdownDTO(taskHealth, projectHealth, goalHealth));

        // Critical alerts
        List<String> alerts = new ArrayList<>();
        if (overdueTasks > 0) {
            alerts.add(overdueTasks + " high-priority task(s) are overdue and require immediate attention.");
        }
        if (blockedTasks > 0) {
            alerts.add(blockedTasks + " task(s) are currently BLOCKED by prerequisite dependencies.");
        }
        long overdueProjects = projects.stream().filter(p -> p.getEndDate() != null && !"COMPLETED".equals(p.getStatus()) && p.getEndDate().isBefore(LocalDate.now())).count();
        if (overdueProjects > 0) {
            alerts.add(overdueProjects + " project(s) have passed their scheduled end dates.");
        }
        if (alerts.isEmpty()) {
            alerts.add("Operations running smoothly across all teams. No critical blockers detected.");
        }
        dto.setCriticalAlerts(alerts);

        // Recent Audit Activity
        dto.setRecentActivity(auditService.getRecentLogs());

        // My tasks for current user
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            userRepository.findById(currentUserId).ifPresent(u -> {
                if (u.getEmployeeId() != null) {
                    dto.setMyTasks(taskService.getAllTasks(null, u.getEmployeeId(), null));
                }
            });
        }
        if (dto.getMyTasks() == null) {
            dto.setMyTasks(new ArrayList<>());
        }

        return dto;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).map(User::getUserId).orElse(null);
        }
        return null;
    }
}
