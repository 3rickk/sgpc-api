package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para relatório de estoque de materiais.
 * 
 * Contém informações detalhadas sobre a situação do estoque:
 * - Quantidades atual e mínima
 * - Indicadores de baixo estoque
 * - Valores financeiros (custo unitário e total)
 * - Informações do fornecedor
 * - Data de última atualização
 * 
 * Essencial para:
 * - Controle de reposição
 * - Análise de valor imobilizado
 * - Planejamento de compras
 * - Gestão de fornecedores
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório de estoque de um material")
public class StockReportDto {

    @Schema(description = "ID do material", example = "1")
    private Long materialId;

    @Schema(description = "Nome do material", example = "Cimento Portland CP-II")
    private String materialName;

    @Schema(description = "Categoria do material", example = "Cimento")
    private String category;

    @Schema(description = "Unidade de medida", example = "saco")
    private String unit;

    @Schema(description = "Quantidade atual em estoque", example = "150")
    private Integer currentQuantity;

    @Schema(description = "Quantidade mínima recomendada", example = "50")
    private Integer minimumQuantity;

    @Schema(description = "Indica se está com baixo estoque", example = "false")
    private Boolean lowStock;

    @Schema(description = "Custo unitário do material", example = "28.50")
    private BigDecimal unitCost;

    @Schema(description = "Valor total do material em estoque", example = "4275.00")
    private BigDecimal totalValue;

    @Schema(description = "Data da última atualização", example = "2024-03-15")
    private LocalDate lastUpdated;

    @Schema(description = "Nome do fornecedor", example = "Cimentos Brasil Ltda")
    private String supplier;
} 