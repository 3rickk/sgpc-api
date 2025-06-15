package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateDto {

    @NotBlank(message = "Título da tarefa é obrigatório")
    @Size(min = 3, max = 255, message = "Título deve ter entre 3 e 255 caracteres")
    private String title;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String description;

    private TaskStatus status = TaskStatus.A_FAZER;

    private LocalDate startDatePlanned;

    private LocalDate endDatePlanned;

    private LocalDate startDateActual;

    private LocalDate endDateActual;

    @Min(value = 0, message = "Progresso deve ser entre 0 e 100")
    @Max(value = 100, message = "Progresso deve ser entre 0 e 100")
    private Integer progressPercentage = 0;

    @Min(value = 1, message = "Prioridade deve ser entre 1 e 4")
    @Max(value = 4, message = "Prioridade deve ser entre 1 e 4")
    @NotNull(message = "Prioridade é obrigatória")
    private Integer priority = 1;

    @Min(value = 0, message = "Horas estimadas deve ser positivo")
    private Integer estimatedHours;

    @Min(value = 0, message = "Horas reais deve ser positivo")
    private Integer actualHours;

    @Size(max = 3000, message = "Notas devem ter no máximo 3000 caracteres")
    private String notes;

    private Long assignedUserId;
} 