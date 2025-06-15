package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CostReportDto {
    private Long projectId;
    private String projectName;
    private String client;
    private BigDecimal totalBudget;
    private BigDecimal materialCosts;
    private BigDecimal serviceCosts;
    private BigDecimal totalCosts;
    private BigDecimal remainingBudget;
    private Double budgetUtilizationPercent;
    private Boolean overBudget;
    private LocalDate lastUpdated;

    // Construtores
    public CostReportDto() {}

    public CostReportDto(Long projectId, String projectName, String client, BigDecimal totalBudget,
                        BigDecimal materialCosts, BigDecimal serviceCosts, BigDecimal totalCosts,
                        BigDecimal remainingBudget, Double budgetUtilizationPercent, Boolean overBudget,
                        LocalDate lastUpdated) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.client = client;
        this.totalBudget = totalBudget;
        this.materialCosts = materialCosts;
        this.serviceCosts = serviceCosts;
        this.totalCosts = totalCosts;
        this.remainingBudget = remainingBudget;
        this.budgetUtilizationPercent = budgetUtilizationPercent;
        this.overBudget = overBudget;
        this.lastUpdated = lastUpdated;
    }

    // Getters e Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getMaterialCosts() {
        return materialCosts;
    }

    public void setMaterialCosts(BigDecimal materialCosts) {
        this.materialCosts = materialCosts;
    }

    public BigDecimal getServiceCosts() {
        return serviceCosts;
    }

    public void setServiceCosts(BigDecimal serviceCosts) {
        this.serviceCosts = serviceCosts;
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public Double getBudgetUtilizationPercent() {
        return budgetUtilizationPercent;
    }

    public void setBudgetUtilizationPercent(Double budgetUtilizationPercent) {
        this.budgetUtilizationPercent = budgetUtilizationPercent;
    }

    public Boolean getOverBudget() {
        return overBudget;
    }

    public void setOverBudget(Boolean overBudget) {
        this.overBudget = overBudget;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 