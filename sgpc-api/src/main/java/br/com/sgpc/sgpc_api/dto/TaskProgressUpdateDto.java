package br.com.sgpc.sgpc_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressUpdateDto {
    
    @NotNull(message = "Percentual de progresso é obrigatório")
    @Min(value = 0, message = "Progresso deve ser no mínimo 0%")
    @Max(value = 100, message = "Progresso deve ser no máximo 100%")
    private Integer progressPercentage;
    
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String notes;
    
    private Integer actualHours; // Horas reais trabalhadas
} 