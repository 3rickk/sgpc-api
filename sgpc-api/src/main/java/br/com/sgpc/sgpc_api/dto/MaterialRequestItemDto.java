package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para item detalhado de solicitação de material.
 * 
 * Representa um material específico dentro de uma solicitação
 * com informações completas incluindo preços e cálculos.
 * Usado na visualização de detalhes da solicitação.
 * 
 * Diferença do MaterialRequestCreateItemDto:
 * - Este inclui preços e dados calculados
 * - MaterialRequestCreateItemDto é apenas para criação
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item detalhado de uma solicitação de material")
public class MaterialRequestItemDto {

    @Schema(description = "ID único do item na solicitação", example = "1")
    private Long id;

    @Schema(description = "ID do material", example = "1")
    private Long materialId;

    @Schema(description = "Nome do material", example = "Cimento Portland CP-II")
    private String materialName;

    @Schema(description = "Unidade de medida do material", example = "saco")
    private String materialUnitOfMeasure;

    @Schema(description = "Quantidade solicitada", example = "50.0")
    private BigDecimal quantity;

    @Schema(description = "Preço unitário do material", example = "28.50")
    private BigDecimal unitPrice;

    @Schema(description = "Preço total (quantidade * preço unitário)", example = "1425.00")
    private BigDecimal totalPrice;

    @Schema(description = "Observações específicas para este item", 
            example = "Preferência por marca Votorantim")
    private String observations;
} 