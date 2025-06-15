package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectReportDto {
    private Long id;
    private String name;
    private String client;
    private LocalDate startDatePlanned;
    private LocalDate endDatePlanned;
    private LocalDate startDateActual;
    private LocalDate endDateActual;
    private String status;
    private Integer progressPercentage;
    private BigDecimal totalBudget;
    private BigDecimal usedBudget;
    private Integer totalTasks;
    private Integer completedTasks;
    private Boolean delayed;
    private Long daysRemaining;

    // Construtores
    public ProjectReportDto() {}

    public ProjectReportDto(Long id, String name, String client, LocalDate startDatePlanned,
                           LocalDate endDatePlanned, LocalDate startDateActual, LocalDate endDateActual,
                           String status, Integer progressPercentage, BigDecimal totalBudget, 
                           BigDecimal usedBudget, Integer totalTasks, Integer completedTasks, 
                           Boolean delayed, Long daysRemaining) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.startDatePlanned = startDatePlanned;
        this.endDatePlanned = endDatePlanned;
        this.startDateActual = startDateActual;
        this.endDateActual = endDateActual;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.totalBudget = totalBudget;
        this.usedBudget = usedBudget;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.delayed = delayed;
        this.daysRemaining = daysRemaining;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public LocalDate getStartDatePlanned() {
        return startDatePlanned;
    }

    public void setStartDatePlanned(LocalDate startDatePlanned) {
        this.startDatePlanned = startDatePlanned;
    }

    public LocalDate getEndDatePlanned() {
        return endDatePlanned;
    }

    public void setEndDatePlanned(LocalDate endDatePlanned) {
        this.endDatePlanned = endDatePlanned;
    }

    public LocalDate getStartDateActual() {
        return startDateActual;
    }

    public void setStartDateActual(LocalDate startDateActual) {
        this.startDateActual = startDateActual;
    }

    public LocalDate getEndDateActual() {
        return endDateActual;
    }

    public void setEndDateActual(LocalDate endDateActual) {
        this.endDateActual = endDateActual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
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

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Integer completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Boolean getDelayed() {
        return delayed;
    }

    public void setDelayed(Boolean delayed) {
        this.delayed = delayed;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
} 