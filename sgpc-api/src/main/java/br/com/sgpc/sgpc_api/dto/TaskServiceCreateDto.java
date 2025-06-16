package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de serviços associados a tarefas.
 * 
 * Este DTO define os dados necessários para associar um serviço a uma tarefa,
 * incluindo quantidade, custos personalizados e observações.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de serviço associado a tarefa")
public class TaskServiceCreateDto {
    
    /**
     * ID do serviço a ser associado à tarefa.
     */
    @Schema(
        description = "ID do serviço",
        example = "1",
        required = true
    )
    @NotNull(message = "ID do serviço é obrigatório")
    private Long serviceId;
    
    /**
     * Quantidade do serviço a ser executada.
     */
    @Schema(
        description = "Quantidade do serviço",
        example = "10.5",
        minimum = "0.01",
        required = true
    )
    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;
    
    /**
     * Custo unitário personalizado (substitui o custo padrão do serviço).
     */
    @Schema(
        description = "Custo unitário personalizado para este serviço",
        example = "150.00",
        minimum = "0.0"
    )
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo unitário personalizado deve ser positivo")
    private BigDecimal unitCostOverride;
    
    /**
     * Observações sobre o serviço na tarefa.
     */
    @Schema(
        description = "Observações sobre o serviço",
        example = "Serviço especial com material premium",
        maxLength = 1000
    )
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String notes;
} 