package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de projetos.
 * 
 * Contém todos os campos opcionais que podem ser atualizados em um projeto.
 * Apenas os campos fornecidos serão atualizados no banco de dados.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de um projeto existente")
public class ProjectUpdateDto {

    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova - Fase 2")
    private String name;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do projeto", 
            example = "Projeto ampliado para incluir piscina e área gourmet")
    private String description;

    @Schema(description = "Data planejada de início", example = "2024-02-01")
    private LocalDate startDatePlanned;

    @Schema(description = "Data planejada de conclusão", example = "2024-08-15")
    private LocalDate endDatePlanned;

    @Schema(description = "Data real de início", example = "2024-02-05")
    private LocalDate startDateActual;

    @Schema(description = "Data real de conclusão", example = "2024-08-20")
    private LocalDate endDateActual;

    @Schema(description = "Orçamento total atualizado", example = "280000.00")
    private BigDecimal totalBudget;

    @Size(max = 255, message = "Nome do cliente deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do cliente", example = "João Silva")
    private String client;

    @Schema(description = "Status do projeto")
    private ProjectStatus status;

    @Schema(description = "IDs dos membros da equipe", 
            example = "[1, 3, 5, 7, 9]")
    private Set<Long> teamMemberIds;
} 