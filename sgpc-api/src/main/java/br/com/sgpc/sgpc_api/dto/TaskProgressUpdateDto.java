package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de progresso de tarefas.
 * 
 * Este DTO permite atualizar o progresso de uma tarefa específica,
 * incluindo percentual de conclusão, observações e horas trabalhadas.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de progresso de tarefa")
public class TaskProgressUpdateDto {
    
    /**
     * Percentual de progresso da tarefa (0-100%).
     */
    @Schema(
        description = "Percentual de progresso da tarefa",
        example = "75",
        minimum = "0",
        maximum = "100",
        required = true
    )
    @NotNull(message = "Percentual de progresso é obrigatório")
    @Min(value = 0, message = "Progresso deve ser no mínimo 0%")
    @Max(value = 100, message = "Progresso deve ser no máximo 100%")
    private Integer progressPercentage;
    
    /**
     * Observações sobre o progresso realizado.
     */
    @Schema(
        description = "Observações sobre o progresso",
        example = "Concluída a fase de fundação. Iniciando estrutura de concreto.",
        maxLength = 1000
    )
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String notes;
    
    /**
     * Horas reais trabalhadas na tarefa.
     */
    @Schema(
        description = "Horas reais trabalhadas na tarefa",
        example = "8",
        minimum = "0"
    )
    private Integer actualHours;
} 