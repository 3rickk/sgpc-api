package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

public class DashboardDto {
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer delayedProjects;
    private Integer pendingTasks;
    private Integer pendingMaterialRequests;
    private Integer lowStockAlerts;
    private BigDecimal totalBudget;
    private BigDecimal usedBudget;
    private Integer completedTasksThisMonth;
    private Integer newProjectsThisMonth;

    // Construtores
    public DashboardDto() {}

    public DashboardDto(Integer totalProjects, Integer activeProjects, Integer delayedProjects, 
                       Integer pendingTasks, Integer pendingMaterialRequests, Integer lowStockAlerts,
                       BigDecimal totalBudget, BigDecimal usedBudget, Integer completedTasksThisMonth,
                       Integer newProjectsThisMonth) {
        this.totalProjects = totalProjects;
        this.activeProjects = activeProjects;
        this.delayedProjects = delayedProjects;
        this.pendingTasks = pendingTasks;
        this.pendingMaterialRequests = pendingMaterialRequests;
        this.lowStockAlerts = lowStockAlerts;
        this.totalBudget = totalBudget;
        this.usedBudget = usedBudget;
        this.completedTasksThisMonth = completedTasksThisMonth;
        this.newProjectsThisMonth = newProjectsThisMonth;
    }

    // Getters e Setters
    public Integer getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(Integer totalProjects) {
        this.totalProjects = totalProjects;
    }

    public Integer getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(Integer activeProjects) {
        this.activeProjects = activeProjects;
    }

    public Integer getDelayedProjects() {
        return delayedProjects;
    }

    public void setDelayedProjects(Integer delayedProjects) {
        this.delayedProjects = delayedProjects;
    }

    public Integer getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(Integer pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public Integer getPendingMaterialRequests() {
        return pendingMaterialRequests;
    }

    public void setPendingMaterialRequests(Integer pendingMaterialRequests) {
        this.pendingMaterialRequests = pendingMaterialRequests;
    }

    public Integer getLowStockAlerts() {
        return lowStockAlerts;
    }

    public void setLowStockAlerts(Integer lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getUsedBudget() {
        return usedBudget;
    }

    public void setUsedBudget(BigDecimal usedBudget) {
        this.usedBudget = usedBudget;
    }

    public Integer getCompletedTasksThisMonth() {
        return completedTasksThisMonth;
    }

    public void setCompletedTasksThisMonth(Integer completedTasksThisMonth) {
        this.completedTasksThisMonth = completedTasksThisMonth;
    }

    public Integer getNewProjectsThisMonth() {
        return newProjectsThisMonth;
    }

    public void setNewProjectsThisMonth(Integer newProjectsThisMonth) {
        this.newProjectsThisMonth = newProjectsThisMonth;
    }
} 