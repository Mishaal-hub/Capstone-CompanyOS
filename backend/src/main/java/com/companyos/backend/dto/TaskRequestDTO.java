package com.companyos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TaskRequestDTO {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;
    private Long assigneeId;
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    private String status = "TODO"; // TODO, IN_PROGRESS, BLOCKED, REVIEW, COMPLETED
    private LocalDate dueDate;
    private Double estimatedHours;
    private Double actualHours;
    private List<Long> dependsOnTaskIds;

    public TaskRequestDTO() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

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

    public List<Long> getDependsOnTaskIds() { return dependsOnTaskIds; }
    public void setDependsOnTaskIds(List<Long> dependsOnTaskIds) { this.dependsOnTaskIds = dependsOnTaskIds; }
}
