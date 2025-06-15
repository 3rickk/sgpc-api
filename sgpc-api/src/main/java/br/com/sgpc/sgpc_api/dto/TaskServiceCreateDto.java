package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskServiceCreateDto {
    
    @NotNull(message = "ID do serviço é obrigatório")
    private Long serviceId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo unitário personalizado deve ser positivo")
    private BigDecimal unitCostOverride;
    
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String notes;
} 