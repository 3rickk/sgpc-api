package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para relatório de custos de projetos.
 * 
 * Contém análise financeira detalhada incluindo:
 * - Orçamento vs gastos realizados
 * - Separação por categorias de custo
 * - Percentual de utilização do orçamento
 * - Indicadores de estouro orçamentário
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório de custos de um projeto")
public class CostReportDto {

    @Schema(description = "ID do projeto", example = "1")
    private Long projectId;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String projectName;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String client;

    @Schema(description = "Orçamento total aprovado", example = "250000.00")
    private BigDecimal totalBudget;

    @Schema(description = "Custos com materiais", example = "97500.00")
    private BigDecimal materialCosts;

    @Schema(description = "Custos com serviços", example = "65000.00")
    private BigDecimal serviceCosts;

    @Schema(description = "Total de custos realizados", example = "162500.00")
    private BigDecimal totalCosts;

    @Schema(description = "Orçamento restante", example = "87500.00")
    private BigDecimal remainingBudget;

    @Schema(description = "Percentual de utilização do orçamento", example = "65.0")
    private Double budgetUtilizationPercent;

    @Schema(description = "Indica se estourou o orçamento", example = "false")
    private Boolean overBudget;

    @Schema(description = "Data da última atualização", example = "2024-03-15")
    private LocalDate lastUpdated;
} 