package com.companyos.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TaskDependency — models predecessor/successor blocking relationships between tasks.
 */
@Entity
@Table(name = "task_dependencies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "depends_on_task_id"})
})
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId; // The dependent task that is blocked/waiting

    @Column(name = "depends_on_task_id", nullable = false)
    private Long dependsOnTaskId; // The prerequisite task that must finish first

    @Column(name = "dependency_type", nullable = false)
    private String dependencyType = "BLOCKS"; // BLOCKS, REQUIRES

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public TaskDependency() {}

    public TaskDependency(Long taskId, Long dependsOnTaskId, String dependencyType) {
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
        this.dependencyType = dependencyType != null ? dependencyType : "BLOCKS";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getDependsOnTaskId() { return dependsOnTaskId; }
    public void setDependsOnTaskId(Long dependsOnTaskId) { this.dependsOnTaskId = dependsOnTaskId; }

    public String getDependencyType() { return dependencyType; }
    public void setDependencyType(String dependencyType) { this.dependencyType = dependencyType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
