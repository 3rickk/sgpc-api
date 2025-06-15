package br.com.sgpc.sgpc_api.dto;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateStatusDto {

    @NotNull(message = "Status é obrigatório")
    private TaskStatus status;

    private String notes; // Notas opcionais sobre a mudança de status
} 