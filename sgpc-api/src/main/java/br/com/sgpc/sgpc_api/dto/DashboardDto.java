package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados consolidados do dashboard principal.
 * 
 * Esta classe contém todas as informações estatísticas e resumos
 * necessários para exibição no dashboard principal do sistema,
 * incluindo métricas de projetos, tarefas, materiais e outras
 * informações relevantes para tomada de decisão.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados consolidados para o dashboard principal do sistema")
public class DashboardDto {
    
    @Schema(description = "Número total de projetos no sistema", example = "25")
    private Integer totalProjects;
    
    @Schema(description = "Número de projetos ativos (em andamento)", example = "12")
    private Integer activeProjects;
    
    @Schema(description = "Número de projetos concluídos", example = "8")
    private Integer completedProjects;
    
    @Schema(description = "Número de projetos pausados", example = "3")
    private Integer pausedProjects;
    
    @Schema(description = "Número de projetos cancelados", example = "2")
    private Integer cancelledProjects;
    
    @Schema(description = "Número total de tarefas no sistema", example = "156")
    private Integer totalTasks;
    
    @Schema(description = "Número de tarefas não iniciadas", example = "45")
    private Integer notStartedTasks;
    
    @Schema(description = "Número de tarefas em progresso", example = "67")
    private Integer inProgressTasks;
    
    @Schema(description = "Número de tarefas concluídas", example = "44")
    private Integer completedTasks;
    
    @Schema(description = "Número total de materiais cadastrados", example = "89")
    private Integer totalMaterials;
    
    @Schema(description = "Número de materiais com baixo estoque", example = "12")
    private Integer lowStockMaterials;
    
    @Schema(description = "Número de solicitações de material pendentes", example = "8")
    private Integer pendingMaterialRequests;
    
    @Schema(description = "Número total de usuários no sistema", example = "23")
    private Integer totalUsers;
    
    @Schema(description = "Número de usuários ativos", example = "21")
    private Integer activeUsers;
    
    @Schema(description = "Custo total estimado de todos os projetos", example = "1250000.00")
    private BigDecimal totalEstimatedCost;
    
    @Schema(description = "Custo total realizado de todos os projetos", example = "687500.50")
    private BigDecimal totalRealizedCost;
    
    @Schema(description = "Percentual médio de progresso dos projetos ativos", example = "65.5")
    private Double averageProjectProgress;
    
    @Schema(description = "Valor total do orçamento alocado", example = "1500000.00")
    private BigDecimal totalBudgetAllocated;
    
    @Schema(description = "Número de projetos com orçamento estourado", example = "3")
    private Integer overBudgetProjects;
    
    @Schema(description = "Lista dos 5 projetos mais urgentes")
    private List<ProjectSummaryDto> urgentProjects;
    
    @Schema(description = "Lista dos 5 materiais com menor estoque")
    private List<MaterialDto> criticalStockMaterials;
    
    @Schema(description = "Estatísticas dos últimos 30 dias")
    private MonthlyStatsDto monthlyStats;
    
    /**
     * DTO interno para estatísticas mensais.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estatísticas dos últimos 30 dias")
    public static class MonthlyStatsDto {
        
        @Schema(description = "Número de projetos criados no mês", example = "4")
        private Integer projectsCreated;
        
        @Schema(description = "Número de projetos concluídos no mês", example = "2")
        private Integer projectsCompleted;
        
        @Schema(description = "Número de tarefas concluídas no mês", example = "28")
        private Integer tasksCompleted;
        
        @Schema(description = "Número de solicitações de material aprovadas no mês", example = "15")
        private Integer materialRequestsApproved;
        
        @Schema(description = "Custo total gasto no mês", example = "125000.75")
        private BigDecimal totalSpentThisMonth;
        
        @Schema(description = "Comparação percentual com o mês anterior", example = "+12.5")
        private Double percentageChangeFromLastMonth;
    }
} 