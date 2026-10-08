package com.companyos.backend.dto;

public class TaskDependencyDTO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long dependsOnTaskId;
    private String dependsOnTaskTitle;
    private String dependsOnTaskStatus;
    private String dependencyType;

    public TaskDependencyDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public Long getDependsOnTaskId() { return dependsOnTaskId; }
    public void setDependsOnTaskId(Long dependsOnTaskId) { this.dependsOnTaskId = dependsOnTaskId; }

    public String getDependsOnTaskTitle() { return dependsOnTaskTitle; }
    public void setDependsOnTaskTitle(String dependsOnTaskTitle) { this.dependsOnTaskTitle = dependsOnTaskTitle; }

    public String getDependsOnTaskStatus() { return dependsOnTaskStatus; }
    public void setDependsOnTaskStatus(String dependsOnTaskStatus) { this.dependsOnTaskStatus = dependsOnTaskStatus; }

    public String getDependencyType() { return dependencyType; }
    public void setDependencyType(String dependencyType) { this.dependencyType = dependencyType; }
}
