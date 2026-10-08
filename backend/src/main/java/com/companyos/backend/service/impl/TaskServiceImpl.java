package com.companyos.backend.service.impl;

import com.companyos.backend.dto.TaskCommentDTO;
import com.companyos.backend.dto.TaskDependencyDTO;
import com.companyos.backend.dto.TaskRequestDTO;
import com.companyos.backend.dto.TaskResponseDTO;
import com.companyos.backend.entity.Project;
import com.companyos.backend.entity.Task;
import com.companyos.backend.entity.TaskComment;
import com.companyos.backend.entity.TaskDependency;
import com.companyos.backend.entity.User;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.EmployeeRepository;
import com.companyos.backend.repository.ProjectRepository;
import com.companyos.backend.repository.TaskCommentRepository;
import com.companyos.backend.repository.TaskDependencyRepository;
import com.companyos.backend.repository.TaskRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final TaskCommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            TaskCommentRepository commentRepository,
            ProjectRepository projectRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.commentRepository = commentRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(Long projectId, Long assigneeId, String status) {
        Long orgId = TenantContext.getRequiredTenantId();
        List<Task> tasks;

        if (projectId != null) {
            tasks = taskRepository.findByProjectId(projectId).stream()
                    .filter(t -> t.getOrganizationId().equals(orgId))
                    .collect(Collectors.toList());
        } else if (assigneeId != null) {
            tasks = taskRepository.findByOrganizationIdAndAssigneeId(orgId, assigneeId);
        } else if (status != null && !status.isBlank()) {
            tasks = taskRepository.findByOrganizationIdAndStatus(orgId, status.toUpperCase());
        } else {
            tasks = taskRepository.findByOrganizationId(orgId);
        }

        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Task task = taskRepository.findByTaskIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return mapToDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        Long orgId = TenantContext.getRequiredTenantId();

        // Verify project belongs to organization
        Project project = projectRepository.findByProjectIdAndOrganizationId(request.getProjectId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Long currentUserId = getCurrentUserId();

        Task task = new Task();
        task.setOrganizationId(orgId);
        task.setProjectId(project.getProjectId());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(currentUserId);
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setStatus(request.getStatus() != null ? request.getStatus() : "TODO");
        task.setDueDate(request.getDueDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(request.getActualHours() != null ? request.getActualHours() : 0.0);

        Task saved = taskRepository.save(task);

        if (request.getDependsOnTaskIds() != null) {
            for (Long depId : request.getDependsOnTaskIds()) {
                if (!depId.equals(saved.getTaskId())) {
                    dependencyRepository.save(new TaskDependency(saved.getTaskId(), depId, "BLOCKS"));
                }
            }
        }

        updateProjectProgress(project.getProjectId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Long orgId = TenantContext.getRequiredTenantId();
        Task task = taskRepository.findByTaskIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setEstimatedHours(request.getEstimatedHours());
        if (request.getActualHours() != null) task.setActualHours(request.getActualHours());

        Task updated = taskRepository.save(task);
        updateProjectProgress(task.getProjectId());
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long id, String status) {
        Long orgId = TenantContext.getRequiredTenantId();
        Task task = taskRepository.findByTaskIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setStatus(status.toUpperCase().trim());
        Task updated = taskRepository.save(task);
        updateProjectProgress(task.getProjectId());
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Task task = taskRepository.findByTaskIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Clean dependencies
        dependencyRepository.findByTaskId(id).forEach(dependencyRepository::delete);
        dependencyRepository.findByDependsOnTaskId(id).forEach(dependencyRepository::delete);

        taskRepository.delete(task);
        updateProjectProgress(task.getProjectId());
    }

    @Override
    @Transactional
    public TaskDependencyDTO addDependency(Long taskId, Long dependsOnTaskId, String dependencyType) {
        Long orgId = TenantContext.getRequiredTenantId();

        if (taskId.equals(dependsOnTaskId)) {
            throw new IllegalArgumentException("A task cannot depend on itself.");
        }

        taskRepository.findByTaskIdAndOrganizationId(taskId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        taskRepository.findByTaskIdAndOrganizationId(dependsOnTaskId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite Task", "id", dependsOnTaskId));

        TaskDependency dependency = dependencyRepository.findByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)
                .orElseGet(() -> dependencyRepository.save(new TaskDependency(taskId, dependsOnTaskId, dependencyType)));

        TaskDependencyDTO dto = new TaskDependencyDTO();
        dto.setId(dependency.getId());
        dto.setTaskId(taskId);
        dto.setDependsOnTaskId(dependsOnTaskId);
        dto.setDependencyType(dependency.getDependencyType());
        return dto;
    }

    @Override
    @Transactional
    public void removeDependency(Long taskId, Long dependsOnTaskId) {
        dependencyRepository.deleteByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId);
    }

    @Override
    @Transactional
    public TaskCommentDTO addComment(Long taskId, String content) {
        Long orgId = TenantContext.getRequiredTenantId();
        taskRepository.findByTaskIdAndOrganizationId(taskId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long currentUserId = getCurrentUserId();
        String authorName = "System User";
        if (currentUserId != null) {
            authorName = userRepository.findById(currentUserId)
                    .map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("System User");
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(currentUserId);
        comment.setAuthorName(authorName);
        comment.setContent(content.trim());

        TaskComment saved = commentRepository.save(comment);

        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setCommentId(saved.getCommentId());
        dto.setTaskId(saved.getTaskId());
        dto.setUserId(saved.getUserId());
        dto.setAuthorName(saved.getAuthorName());
        dto.setContent(saved.getContent());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentDTO> getComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(c -> {
            TaskCommentDTO dto = new TaskCommentDTO();
            dto.setCommentId(c.getCommentId());
            dto.setTaskId(c.getTaskId());
            dto.setUserId(c.getUserId());
            dto.setAuthorName(c.getAuthorName());
            dto.setContent(c.getContent());
            dto.setCreatedAt(c.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    private void updateProjectProgress(Long projectId) {
        projectRepository.findById(projectId).ifPresent(p -> {
            var tasks = taskRepository.findByProjectId(projectId);
            if (!tasks.isEmpty()) {
                long completed = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
                int progress = (int) Math.round(((double) completed / tasks.size()) * 100);
                p.setProgress(progress);
                if (progress == 100 && !"COMPLETED".equals(p.getStatus())) {
                    p.setStatus("COMPLETED");
                } else if (progress > 0 && "PLANNED".equals(p.getStatus())) {
                    p.setStatus("IN_PROGRESS");
                }
                projectRepository.save(p);
            }
        });
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).map(User::getUserId).orElse(null);
        }
        return null;
    }

    private TaskResponseDTO mapToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setTaskId(task.getTaskId());
        dto.setOrganizationId(task.getOrganizationId());
        dto.setProjectId(task.getProjectId());

        projectRepository.findById(task.getProjectId()).ifPresent(p -> dto.setProjectName(p.getProjectName()));

        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setAssigneeId(task.getAssigneeId());

        if (task.getAssigneeId() != null) {
            employeeRepository.findById(task.getAssigneeId()).ifPresent(emp -> {
                dto.setAssigneeName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            });
        }

        dto.setCreatorId(task.getCreatorId());
        if (task.getCreatorId() != null) {
            userRepository.findById(task.getCreatorId()).ifPresent(u -> {
                dto.setCreatorName(u.getFullName() != null ? u.getFullName() : u.getUsername());
            });
        }

        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());

        boolean isOverdue = task.getDueDate() != null &&
                !"COMPLETED".equals(task.getStatus()) &&
                task.getDueDate().isBefore(LocalDate.now());
        dto.setOverdue(isOverdue);

        // Dependencies (prerequisites this task depends on)
        List<TaskDependencyDTO> deps = new ArrayList<>();
        for (TaskDependency dep : dependencyRepository.findByTaskId(task.getTaskId())) {
            taskRepository.findById(dep.getDependsOnTaskId()).ifPresent(prereq -> {
                TaskDependencyDTO ddto = new TaskDependencyDTO();
                ddto.setId(dep.getId());
                ddto.setTaskId(task.getTaskId());
                ddto.setTaskTitle(task.getTitle());
                ddto.setDependsOnTaskId(prereq.getTaskId());
                ddto.setDependsOnTaskTitle(prereq.getTitle());
                ddto.setDependsOnTaskStatus(prereq.getStatus());
                ddto.setDependencyType(dep.getDependencyType());
                deps.add(ddto);
            });
        }
        dto.setDependencies(deps);

        // Blocking tasks (tasks that are waiting on this task)
        List<TaskDependencyDTO> blocking = new ArrayList<>();
        for (TaskDependency dep : dependencyRepository.findByDependsOnTaskId(task.getTaskId())) {
            taskRepository.findById(dep.getTaskId()).ifPresent(depTask -> {
                TaskDependencyDTO ddto = new TaskDependencyDTO();
                ddto.setId(dep.getId());
                ddto.setTaskId(depTask.getTaskId());
                ddto.setTaskTitle(depTask.getTitle());
                ddto.setDependsOnTaskId(task.getTaskId());
                ddto.setDependsOnTaskTitle(task.getTitle());
                ddto.setDependsOnTaskStatus(task.getStatus());
                ddto.setDependencyType(dep.getDependencyType());
                blocking.add(ddto);
            });
        }
        dto.setBlockingTasks(blocking);

        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }
}
