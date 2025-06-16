package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resumo de projetos.
 * 
 * Contém informações essenciais do projeto para listagens e sumários.
 * Usado em endpoints que retornam múltiplos projetos onde detalhes
 * completos não são necessários.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de um projeto de construção")
public class ProjectSummaryDto {

    @Schema(description = "ID único do projeto", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String name;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String client;

    @Schema(description = "Status atual do projeto")
    private ProjectStatus status;

    @Schema(description = "Descrição textual do status", example = "Em Andamento")
    private String statusDescription;

    @Schema(description = "Data planejada de início", example = "2024-01-15")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada de conclusão", example = "2024-06-30")
    private LocalDate endDatePlanned;

    @Schema(description = "Orçamento total do projeto", example = "250000.00")
    private BigDecimal totalBudget;

    @Schema(description = "Data de criação do projeto", example = "2024-01-10T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Número de membros na equipe", example = "5")
    private int teamSize;

    @Schema(description = "Percentual de progresso (0-100)", example = "65")
    private Integer progressPercentage;

    @Schema(description = "Indica se o projeto está atrasado", example = "false")
    private Boolean isDelayed;
} 