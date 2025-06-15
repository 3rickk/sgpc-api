package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto {

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;

    @NotBlank(message = "Tipo de movimentação é obrigatório")
    private String movementType; // "ENTRADA" ou "SAIDA"

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observation;
} 