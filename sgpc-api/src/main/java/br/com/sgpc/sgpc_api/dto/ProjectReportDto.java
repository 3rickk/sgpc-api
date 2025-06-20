package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para relatório consolidado de projetos.
 * 
 * Contém informações completas para análise de projetos incluindo:
 * - Dados básicos do projeto (nome, cliente, datas)
 * - Progresso e status atual
 * - Informações orçamentárias
 * - Métricas de tarefas
 * - Indicadores de desempenho (atraso, dias restantes)
 * 
 * Usado nos relatórios gerenciais para visão consolidada
 * do portfólio de projetos da empresa.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório consolidado de um projeto")
public class ProjectReportDto {

    @Schema(description = "ID único do projeto", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String name;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String client;

    @Schema(description = "Data planejada de início", example = "2024-01-15")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada de conclusão", example = "2024-06-30")
    private LocalDate endDatePlanned;

    @Schema(description = "Data real de início", example = "2024-01-20")
    private LocalDate startDateActual;

    @Schema(description = "Data real de conclusão", example = "2024-07-05")
    private LocalDate endDateActual;

    @Schema(description = "Status atual do projeto", example = "Em Andamento")
    private String status;

    @Schema(description = "Percentual de progresso (0-100)", example = "65")
    private Integer progressPercentage;

    @Schema(description = "Orçamento total aprovado", example = "250000.00")
    private BigDecimal totalBudget;

    @Schema(description = "Orçamento já utilizado", example = "162500.00")
    private BigDecimal usedBudget;

    @Schema(description = "Número total de tarefas", example = "28")
    private Integer totalTasks;

    @Schema(description = "Número de tarefas concluídas", example = "18")
    private Integer completedTasks;

    @Schema(description = "Indica se o projeto está atrasado", example = "false")
    private Boolean delayed;

    @Schema(description = "Dias restantes para conclusão planejada", example = "45")
    private Long daysRemaining;

    @Schema(description = "Descrição do projeto")
    private String description;

    @Schema(description = "Nome do criador do projeto", example = "Maria Gerente")
    private String createdByName;

    @Schema(description = "Data de criação do projeto", example = "2024-01-10")
    private LocalDate createdAt;

    @Schema(description = "Número de membros na equipe", example = "8")
    private Integer teamSize;

    @Schema(description = "Lista dos membros da equipe")
    private List<TeamMemberDto> teamMembers;

    @Schema(description = "Lista das tarefas do projeto")
    private List<TaskSummaryDto> tasks;

    @Schema(description = "Número de tarefas por status")
    private TaskStatusSummary taskStatusSummary;

    @Schema(description = "Análise de custos por categoria")
    private ProjectCostBreakdown costBreakdown;

    @Schema(description = "Indicadores de performance do projeto")
    private ProjectPerformanceMetrics performanceMetrics;

    /**
     * Classe interna para resumo de status das tarefas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumo de tarefas por status")
    public static class TaskStatusSummary {
        @Schema(description = "Tarefas a fazer", example = "5")
        private Integer todoTasks;

        @Schema(description = "Tarefas em andamento", example = "8")
        private Integer inProgressTasks;

        @Schema(description = "Tarefas concluídas", example = "12")
        private Integer completedTasks;

        @Schema(description = "Tarefas em atraso", example = "2")
        private Integer overdueTasks;
    }

    /**
     * Classe interna para detalhamento de custos
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhamento de custos por categoria")
    public static class ProjectCostBreakdown {
        @Schema(description = "Total de custos de mão de obra", example = "45000.00")
        private BigDecimal totalLaborCost;

        @Schema(description = "Total de custos de materiais", example = "78000.00")
        private BigDecimal totalMaterialCost;

        @Schema(description = "Total de custos de equipamentos", example = "25000.00")
        private BigDecimal totalEquipmentCost;

        @Schema(description = "Percentual de mão de obra", example = "30.4")
        private Double laborCostPercentage;

        @Schema(description = "Percentual de materiais", example = "52.7")
        private Double materialCostPercentage;

        @Schema(description = "Percentual de equipamentos", example = "16.9")
        private Double equipmentCostPercentage;
    }

    /**
     * Classe interna para métricas de performance
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Métricas de performance do projeto")
    public static class ProjectPerformanceMetrics {
        @Schema(description = "Variação do cronograma em dias", example = "-5")
        private Long scheduleVariance;

        @Schema(description = "Variação do orçamento", example = "-15000.00")
        private BigDecimal budgetVariance;

        @Schema(description = "Eficiência da equipe (%)", example = "92.5")
        private Double teamEfficiency;

        @Schema(description = "Taxa de conclusão de tarefas no prazo (%)", example = "78.3")
        private Double onTimeCompletionRate;

        @Schema(description = "Produtividade (tarefas por dia)", example = "1.2")
        private Double productivity;

        @Schema(description = "Horas estimadas vs realizadas (%)", example = "105.8")
        private Double hoursVariancePercentage;
    }
} 