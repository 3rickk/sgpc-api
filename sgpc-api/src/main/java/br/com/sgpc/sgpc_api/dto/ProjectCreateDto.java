package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de novos projetos.
 * 
 * Esta classe contém todos os dados necessários para criar um novo
 * projeto de construção no sistema, incluindo informações básicas,
 * cronograma, orçamento e definição inicial da equipe.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um novo projeto de construção")
public class ProjectCreateDto {

    @NotBlank(message = "Nome do projeto é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    @Schema(description = "Nome do projeto", example = "Construção Residencial Villa das Flores", minLength = 3, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do projeto", example = "Projeto de construção de residência unifamiliar com 3 dormitórios, área de 120m², incluindo acabamentos completos", maxLength = 5000)
    private String description;

    @Schema(description = "Data planejada para início do projeto", example = "2024-02-15")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada para conclusão do projeto", example = "2024-08-15")
    private LocalDate endDatePlanned;

    @Schema(description = "Data real de início do projeto", example = "2024-02-20")
    private LocalDate startDateActual;

    @Schema(description = "Data real de conclusão do projeto", example = "2024-08-30")
    private LocalDate endDateActual;

    @Schema(description = "Orçamento total planejado para o projeto", example = "250000.00")
    private BigDecimal totalBudget;

    @Size(max = 255, message = "Nome do cliente deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do cliente/contratante", example = "João Silva Santos", maxLength = 255)
    private String client;

    @NotNull(message = "Status do projeto é obrigatório")
    @Schema(description = "Status inicial do projeto", example = "PLANEJAMENTO", requiredMode = Schema.RequiredMode.REQUIRED)
    private ProjectStatus status;

    @Schema(description = "IDs dos membros da equipe inicial do projeto", example = "[1, 3, 5]")
    private Set<Long> teamMemberIds;
} 