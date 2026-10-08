package com.companyos.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    private String description;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (organizationId == null && companyId != null) {
            organizationId = companyId;
        } else if (companyId == null && organizationId != null) {
            companyId = organizationId;
        }
    }

    public Department() {}

    public Department(String departmentName, String description, Long organizationId) {
        this.departmentName = departmentName;
        this.description = description;
        this.organizationId = organizationId;
        this.companyId = organizationId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompanyId() {
        return companyId != null ? companyId : organizationId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
        if (this.organizationId == null) {
            this.organizationId = companyId;
        }
    }

    public Long getOrganizationId() {
        return organizationId != null ? organizationId : companyId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        if (this.companyId == null) {
            this.companyId = organizationId;
        }
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}