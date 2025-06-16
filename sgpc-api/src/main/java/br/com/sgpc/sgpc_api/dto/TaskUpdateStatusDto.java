package br.com.sgpc.sgpc_api.dto;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de status de tarefas.
 * 
 * Este DTO permite alterar o status de uma tarefa específica,
 * com notas opcionais explicando a mudança.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de status de tarefa")
public class TaskUpdateStatusDto {

    /**
     * Novo status da tarefa.
     */
    @Schema(
        description = "Novo status da tarefa",
        example = "IN_PROGRESS",
        required = true,
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"}
    )
    @NotNull(message = "Status é obrigatório")
    private TaskStatus status;

    /**
     * Notas explicativas sobre a mudança de status.
     */
    @Schema(
        description = "Notas sobre a mudança de status",
        example = "Tarefa iniciada após aprovação do material",
        maxLength = 500
    )
    private String notes; // Notas opcionais sobre a mudança de status
} 