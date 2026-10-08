package com.companyos.backend.dto;

import com.companyos.backend.entity.AuditLog;
import java.util.List;

public class DashboardOverviewDTO {

    private long totalEmployees;
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long pendingTasks;
    private long completedTasks;
    private long overdueTasks;
    private long blockedTasks;
    private long totalClients;
    private double totalPipelineValue;
    private int companyHealthScore;
    private HealthBreakdownDTO healthBreakdown;
    private List<String> criticalAlerts;
    private List<AuditLog> recentActivity;
    private List<TaskResponseDTO> myTasks;

    public static class HealthBreakdownDTO {
        private int taskHealth;
        private int projectHealth;
        private int goalHealth;

        public HealthBreakdownDTO() {}

        public HealthBreakdownDTO(int taskHealth, int projectHealth, int goalHealth) {
            this.taskHealth = taskHealth;
            this.projectHealth = projectHealth;
            this.goalHealth = goalHealth;
        }

        public int getTaskHealth() { return taskHealth; }
        public void setTaskHealth(int taskHealth) { this.taskHealth = taskHealth; }
        public int getProjectHealth() { return projectHealth; }
        public void setProjectHealth(int projectHealth) { this.projectHealth = projectHealth; }
        public int getGoalHealth() { return goalHealth; }
        public void setGoalHealth(int goalHealth) { this.goalHealth = goalHealth; }
    }

    public DashboardOverviewDTO() {}

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }

    public long getActiveProjects() { return activeProjects; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }

    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }

    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }

    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }

    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }

    public long getBlockedTasks() { return blockedTasks; }
    public void setBlockedTasks(long blockedTasks) { this.blockedTasks = blockedTasks; }

    public long getTotalClients() { return totalClients; }
    public void setTotalClients(long totalClients) { this.totalClients = totalClients; }

    public double getTotalPipelineValue() { return totalPipelineValue; }
    public void setTotalPipelineValue(double totalPipelineValue) { this.totalPipelineValue = totalPipelineValue; }

    public int getCompanyHealthScore() { return companyHealthScore; }
    public void setCompanyHealthScore(int companyHealthScore) { this.companyHealthScore = companyHealthScore; }

    public HealthBreakdownDTO getHealthBreakdown() { return healthBreakdown; }
    public void setHealthBreakdown(HealthBreakdownDTO healthBreakdown) { this.healthBreakdown = healthBreakdown; }

    public List<String> getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(List<String> criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public List<AuditLog> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<AuditLog> recentActivity) { this.recentActivity = recentActivity; }

    public List<TaskResponseDTO> getMyTasks() { return myTasks; }
    public void setMyTasks(List<TaskResponseDTO> myTasks) { this.myTasks = myTasks; }
}
