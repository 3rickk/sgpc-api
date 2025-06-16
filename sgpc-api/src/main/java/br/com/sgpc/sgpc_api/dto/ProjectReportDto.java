package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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
} 