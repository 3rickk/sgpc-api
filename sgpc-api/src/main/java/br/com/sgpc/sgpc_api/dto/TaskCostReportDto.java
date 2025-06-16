package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para relatório de custos por tarefa.
 * 
 * Contém análise detalhada dos custos de uma tarefa específica,
 * separando por categoria (mão de obra, material, equipamento).
 * Inclui também o progresso da tarefa e lista de serviços executados.
 * 
 * Usado para:
 * - Análise de rentabilidade por tarefa
 * - Controle de custos em execução
 * - Comparação entre custo planejado vs realizado
 * - Relatórios gerenciais por projeto
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório de custos de uma tarefa")
public class TaskCostReportDto {
    
    @Schema(description = "ID da tarefa", example = "1")
    private Long taskId;

    @Schema(description = "Título da tarefa", example = "Escavação da fundação")
    private String taskTitle;

    @Schema(description = "Custo total com mão de obra", example = "3500.00")
    private BigDecimal laborCost;

    @Schema(description = "Custo total com materiais", example = "2800.00")
    private BigDecimal materialCost;

    @Schema(description = "Custo total com equipamentos", example = "1200.00")
    private BigDecimal equipmentCost;

    @Schema(description = "Custo total da tarefa", example = "7500.00")
    private BigDecimal totalCost;

    @Schema(description = "Percentual de progresso da tarefa (0-100)", example = "75")
    private Integer progressPercentage;

    @Schema(description = "Lista de serviços executados na tarefa")
    private List<TaskServiceDto> services;
} 