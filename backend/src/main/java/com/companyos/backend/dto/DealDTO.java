package com.companyos.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DealDTO {

    private Long dealId;
    private Long organizationId;
    private Long clientId;
    private String clientName;
    private String title;
    private Double value;
    private String stage; // LEAD, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private LocalDate expectedCloseDate;
    private Integer probability;
    private LocalDateTime createdAt;

    public DealDTO() {}

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public Long getAssignedEmployeeId() { return assignedEmployeeId; }
    public void setAssignedEmployeeId(Long assignedEmployeeId) { this.assignedEmployeeId = assignedEmployeeId; }

    public String getAssignedEmployeeName() { return assignedEmployeeName; }
    public void setAssignedEmployeeName(String assignedEmployeeName) { this.assignedEmployeeName = assignedEmployeeName; }

    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(LocalDate expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }

    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
