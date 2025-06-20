package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resumo de tarefas nos relatórios de projetos.
 * 
 * Contém informações essenciais sobre uma tarefa para exibição
 * em relatórios consolidados de projetos.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de uma tarefa para relatórios")
public class TaskSummaryDto {

    @Schema(description = "ID da tarefa", example = "1")
    private Long id;

    @Schema(description = "Título da tarefa", example = "Fundação do prédio")
    private String title;

    @Schema(description = "Status da tarefa", example = "EM_ANDAMENTO")
    private String status;

    @Schema(description = "Percentual de progresso", example = "75")
    private Integer progressPercentage;

    @Schema(description = "Prioridade da tarefa (1-4)", example = "3")
    private Integer priority;

    @Schema(description = "Descrição da prioridade", example = "Alta")
    private String priorityDescription;

    @Schema(description = "Data planejada de início", example = "2024-01-15")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada de conclusão", example = "2024-02-15")
    private LocalDate endDatePlanned;

    @Schema(description = "Data real de início", example = "2024-01-18")
    private LocalDate startDateActual;

    @Schema(description = "Data real de conclusão", example = "2024-02-20")
    private LocalDate endDateActual;

    @Schema(description = "Horas estimadas", example = "120")
    private Integer estimatedHours;

    @Schema(description = "Horas trabalhadas", example = "95")
    private Integer actualHours;

    @Schema(description = "Usuário responsável", example = "João Silva")
    private String assignedUserName;

    @Schema(description = "Custo de mão de obra", example = "5000.00")
    private BigDecimal laborCost;

    @Schema(description = "Custo de materiais", example = "8000.00")
    private BigDecimal materialCost;

    @Schema(description = "Custo de equipamentos", example = "2000.00")
    private BigDecimal equipmentCost;

    @Schema(description = "Custo total da tarefa", example = "15000.00")
    private BigDecimal totalCost;

    @Schema(description = "Indica se a tarefa está atrasada", example = "false")
    private Boolean isOverdue;

    @Schema(description = "Dias de atraso (se aplicável)", example = "0")
    private Long daysOverdue;
} 