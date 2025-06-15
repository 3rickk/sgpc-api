package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateDto {

    @NotBlank(message = "Nome do projeto é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String description;

    private LocalDate startDatePlanned;

    private LocalDate endDatePlanned;

    private LocalDate startDateActual;

    private LocalDate endDateActual;

    private BigDecimal totalBudget;

    @Size(max = 255, message = "Nome do cliente deve ter no máximo 255 caracteres")
    private String client;

    @NotNull(message = "Status do projeto é obrigatório")
    private ProjectStatus status;

    private Set<Long> teamMemberIds;
} 