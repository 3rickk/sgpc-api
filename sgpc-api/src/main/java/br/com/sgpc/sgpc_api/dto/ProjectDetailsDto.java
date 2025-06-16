package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para detalhes completos de um projeto.
 * 
 * Contém informações detalhadas do projeto incluindo dados calculados
 * como progresso, orçamento usado e membros da equipe.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhes completos de um projeto de construção")
public class ProjectDetailsDto {

    @Schema(description = "ID único do projeto", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String name;

    @Schema(description = "Descrição detalhada do projeto", 
            example = "Projeto de construção de residência unifamiliar com 3 quartos, 2 banheiros, sala, cozinha e área de serviço")
    private String description;

    @Schema(description = "Data planejada de início", example = "2024-01-15")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada de conclusão", example = "2024-06-30")
    private LocalDate endDatePlanned;

    @Schema(description = "Data real de início", example = "2024-01-20")
    private LocalDate startDateActual;

    @Schema(description = "Data real de conclusão", example = "2024-07-05")
    private LocalDate endDateActual;

    @Schema(description = "Orçamento total do projeto", example = "250000.00")
    private BigDecimal totalBudget;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String client;

    @Schema(description = "Status atual do projeto")
    private ProjectStatus status;

    @Schema(description = "Descrição textual do status", example = "Em Andamento")
    private String statusDescription;

    @Schema(description = "Data de criação do projeto", example = "2024-01-10T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-03-15T14:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Lista de membros da equipe do projeto")
    private Set<UserDto> teamMembers;

    @Schema(description = "Número total de membros na equipe", example = "5")
    private int teamSize;
    
    // Campos calculados
    @Schema(description = "Percentual de progresso do projeto (0-100)", example = "65")
    private Integer progressPercentage;

    @Schema(description = "Valor já gasto no projeto", example = "162500.00")
    private BigDecimal budgetUsed;

    @Schema(description = "Valor restante do orçamento", example = "87500.00")
    private BigDecimal budgetRemaining;

    @Schema(description = "Indica se o projeto está atrasado", example = "false")
    private Boolean isDelayed;

    @Schema(description = "Dias restantes para conclusão planejada", example = "45")
    private Long daysRemaining;
} 