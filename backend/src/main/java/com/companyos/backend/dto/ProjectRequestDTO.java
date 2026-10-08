package com.companyos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class ProjectRequestDTO {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;
    private Long clientId;
    private Long projectManagerId;
    private Double budget;
    private String priority = "MEDIUM";
    private String status = "PLANNED";
    private Integer progress = 0;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> memberEmployeeIds;

    public ProjectRequestDTO() {}

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getProjectManagerId() { return projectManagerId; }
    public void setProjectManagerId(Long projectManagerId) { this.projectManagerId = projectManagerId; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<Long> getMemberEmployeeIds() { return memberEmployeeIds; }
    public void setMemberEmployeeIds(List<Long> memberEmployeeIds) { this.memberEmployeeIds = memberEmployeeIds; }
}
