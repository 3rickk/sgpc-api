package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de orçamento e controle financeiro de projetos.
 * 
 * Este DTO consolida informações financeiras completas de um projeto,
 * incluindo orçamento, custos realizados, variações e estatísticas.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de orçamento e controle financeiro do projeto")
public class ProjectBudgetDto {
    
    /**
     * ID único do projeto.
     */
    @Schema(description = "ID único do projeto", example = "1")
    private Long projectId;
    
    /**
     * Nome do projeto.
     */
    @Schema(description = "Nome do projeto", example = "Construção Residencial XYZ")
    private String projectName;
    
    /**
     * Orçamento total aprovado para o projeto.
     */
    @Schema(description = "Orçamento total aprovado", example = "150000.00")
    private BigDecimal totalBudget;
    
    /**
     * Custo total realizado até o momento.
     */
    @Schema(description = "Custo total realizado", example = "75000.00")
    private BigDecimal realizedCost;
    
    /**
     * Variação do orçamento (diferença entre orçado e realizado).
     */
    @Schema(description = "Variação do orçamento (realizado - orçado)", example = "-75000.00")
    private BigDecimal budgetVariance;
    
    /**
     * Percentual de utilização do orçamento.
     */
    @Schema(description = "Percentual de utilização do orçamento", example = "50.00")
    private BigDecimal budgetUsagePercentage;
    
    /**
     * Percentual de progresso físico do projeto.
     */
    @Schema(description = "Percentual de progresso físico", example = "45.00")
    private BigDecimal progressPercentage;
    
    /**
     * Indica se o projeto está acima do orçamento.
     */
    @Schema(description = "Indica se está acima do orçamento", example = "false")
    private Boolean isOverBudget;
    
    /**
     * Custo total com mão de obra.
     */
    @Schema(description = "Custo total com mão de obra", example = "30000.00")
    private BigDecimal totalLaborCost;
    
    /**
     * Custo total com materiais.
     */
    @Schema(description = "Custo total com materiais", example = "35000.00")
    private BigDecimal totalMaterialCost;
    
    /**
     * Custo total com equipamentos.
     */
    @Schema(description = "Custo total com equipamentos", example = "10000.00")
    private BigDecimal totalEquipmentCost;
    
    /**
     * Número total de tarefas do projeto.
     */
    @Schema(description = "Número total de tarefas", example = "25")
    private Integer totalTasks;
    
    /**
     * Número de tarefas concluídas.
     */
    @Schema(description = "Número de tarefas concluídas", example = "10")
    private Integer completedTasks;
    
    /**
     * Número de tarefas pendentes.
     */
    @Schema(description = "Número de tarefas pendentes", example = "15")
    private Integer pendingTasks;
} 