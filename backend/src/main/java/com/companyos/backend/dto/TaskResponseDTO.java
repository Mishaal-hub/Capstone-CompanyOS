package com.companyos.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponseDTO {

    private Long taskId;
    private Long organizationId;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private Long assigneeId;
    private String assigneeName;
    private Long creatorId;
    private String creatorName;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private Double estimatedHours;
    private Double actualHours;
    private boolean overdue;
    private List<TaskDependencyDTO> dependencies; // Tasks this task depends on
    private List<TaskDependencyDTO> blockingTasks; // Tasks that depend on this task
    private List<TaskCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskResponseDTO() {}

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public List<TaskDependencyDTO> getDependencies() { return dependencies; }
    public void setDependencies(List<TaskDependencyDTO> dependencies) { this.dependencies = dependencies; }

    public List<TaskDependencyDTO> getBlockingTasks() { return blockingTasks; }
    public void setBlockingTasks(List<TaskDependencyDTO> blockingTasks) { this.blockingTasks = blockingTasks; }

    public List<TaskCommentDTO> getComments() { return comments; }
    public void setComments(List<TaskCommentDTO> comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
