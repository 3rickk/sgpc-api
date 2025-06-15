package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBudgetDto {
    
    private Long projectId;
    private String projectName;
    private BigDecimal totalBudget;
    private BigDecimal realizedCost;
    private BigDecimal budgetVariance;
    private BigDecimal budgetUsagePercentage;
    private BigDecimal progressPercentage;
    private Boolean isOverBudget;
    
    // Custos detalhados
    private BigDecimal totalLaborCost;
    private BigDecimal totalMaterialCost;
    private BigDecimal totalEquipmentCost;
    
    // Estatísticas
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer pendingTasks;
} 