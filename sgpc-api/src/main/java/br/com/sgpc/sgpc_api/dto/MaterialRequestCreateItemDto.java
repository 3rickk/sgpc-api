package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para item de solicitação de material.
 * 
 * Representa um material específico dentro de uma solicitação,
 * incluindo a quantidade necessária e observações particulares
 * para aquele item.
 * 
 * Validações aplicadas:
 * - Material deve existir no cadastro
 * - Quantidade deve ser positiva
 * - Observações são opcionais
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item de uma solicitação de material")
public class MaterialRequestCreateItemDto {
    
    @NotNull(message = "ID do material é obrigatório")
    @Schema(description = "ID do material no cadastro", example = "1", required = true)
    private Long materialId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    @Schema(description = "Quantidade necessária do material", example = "50.0", required = true)
    private BigDecimal quantity;
    
    @Schema(description = "Observações específicas para este item", 
            example = "Preferência por marca Votorantim")
    private String observations;
} 