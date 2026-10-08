package com.companyos.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalDTO {

    private Long goalId;
    private Long organizationId;
    private String title;
    private String description;
    private String level; // ORGANIZATION, DEPARTMENT, TEAM, INDIVIDUAL
    private Long departmentId;
    private String departmentName;
    private Long ownerEmployeeId;
    private String ownerEmployeeName;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    private LocalDate deadline;
    private String status;
    private Integer progress;
    private LocalDateTime createdAt;

    public GoalDTO() {}

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public void setOwnerEmployeeId(Long ownerEmployeeId) { this.ownerEmployeeId = ownerEmployeeId; }

    public String getOwnerEmployeeName() { return ownerEmployeeName; }
    public void setOwnerEmployeeName(String ownerEmployeeName) { this.ownerEmployeeName = ownerEmployeeName; }

    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
