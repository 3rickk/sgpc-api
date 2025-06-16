package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para movimentação de estoque de materiais.
 * 
 * Usado para registrar entradas e saídas de materiais no estoque,
 * permitindo controle preciso das quantidades disponíveis e
 * rastreabilidade das movimentações.
 * 
 * Tipos de movimentação:
 * - ENTRADA: Aumento do estoque (compras, devoluções)
 * - SAIDA: Redução do estoque (utilização em projetos, perdas)
 * 
 * Validações:
 * - Quantidade obrigatória e maior que zero
 * - Tipo de movimentação obrigatório
 * - Observação opcional limitada a 500 caracteres
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para movimentação de estoque de materiais")
public class StockMovementDto {

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    @Schema(description = "Quantidade a ser movimentada", 
            example = "25.5", 
            required = true,
            minimum = "0.01")
    private BigDecimal quantity;

    @NotBlank(message = "Tipo de movimentação é obrigatório")
    @Schema(description = "Tipo da movimentação no estoque", 
            example = "ENTRADA",
            allowableValues = {"ENTRADA", "SAIDA"},
            required = true)
    private String movementType; // "ENTRADA" ou "SAIDA"

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    @Schema(description = "Observações sobre a movimentação", 
            example = "Compra de materiais para projeto Vila Nova - NF 1234",
            maxLength = 500)
    private String observation;
} 