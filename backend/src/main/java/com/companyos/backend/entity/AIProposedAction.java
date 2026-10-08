package com.companyos.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AIProposedAction — models high-risk/sensitive actions proposed by MATE that require human approval.
 */
@Entity
@Table(name = "ai_proposed_actions")
public class AIProposedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "requested_by_user_id")
    private Long requestedByUserId;

    @Column(nullable = false)
    private String actionType; // REASSIGN_TASK, UPDATE_TASK_STATUS, EXTEND_DEADLINE, SEND_NOTIFICATION

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String actionPayload; // JSON string payload

    /**
     * Status: PENDING_APPROVAL, APPROVED, REJECTED, EXECUTED
     */
    @Column(nullable = false)
    private String status = "PENDING_APPROVAL";

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AIProposedAction() {}

    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(Long requestedByUserId) { this.requestedByUserId = requestedByUserId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
