package com.companyos.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "calendar_events", indexes = @Index(name = "idx_calendar_tenant_start", columnList = "organization_id,start_time"))
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable only to quarantine pre-tenant legacy rows; all new writes require a tenant.
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private java.time.Instant startTime;

    @Column(name = "end_time")
    private java.time.Instant endTime;

    @Column(name = "time_zone", length = 100)
    private String timeZone;

    @Column(name = "event_type")
    private String eventType;

    
    private String status;

    
    private String location;

    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt;

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long value) { this.organizationId = value; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long value) { this.createdBy = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { this.username = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public java.time.Instant getStartTime() { return startTime; }
    public void setStartTime(java.time.Instant value) { this.startTime = value; }
    public java.time.Instant getEndTime() { return endTime; }
    public void setEndTime(java.time.Instant value) { this.endTime = value; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String value) { this.timeZone = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { this.eventType = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.Instant value) { this.createdAt = value; }
    public java.time.Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.Instant value) { this.updatedAt = value; }
}
