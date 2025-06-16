package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de materiais existentes.
 * 
 * Esta classe contém os dados que podem ser atualizados em um material
 * já existente no sistema. Todos os campos são opcionais, permitindo
 * atualizações parciais conforme necessário.
 * 
 * Características especiais:
 * - Todos os campos opcionais para updates parciais
 * - Validações apenas quando campos são informados
 * - Mantém integridade dos dados existentes
 * - Preserva histórico de modificações
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de um material existente")
public class MaterialUpdateDto {

    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Nome do material", 
            example = "Cimento Portland CP-III",
            minLength = 2,
            maxLength = 255)
    private String name;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do material", 
            example = "Cimento Portland de Alto Forno, adequado para obras expostas à água",
            maxLength = 5000)
    private String description;

    @Size(min = 1, max = 50, message = "Unidade de medida deve ter entre 1 e 50 caracteres")
    @Schema(description = "Unidade de medida", 
            example = "saco",
            minLength = 1,
            maxLength = 50)
    private String unitOfMeasure;

    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    @Schema(description = "Preço unitário atualizado", 
            example = "32.75",
            minimum = "0.01")
    private BigDecimal unitPrice;

    @Size(max = 255, message = "Nome do fornecedor deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do fornecedor principal", 
            example = "Construção Materiais SA",
            maxLength = 255)
    private String supplier;

    @DecimalMin(value = "0", message = "Estoque mínimo deve ser maior ou igual a zero")
    @Schema(description = "Quantidade mínima em estoque", 
            example = "25",
            minimum = "0")
    private BigDecimal minimumStock;

    @Schema(description = "Status ativo/inativo do material", 
            example = "true")
    private Boolean isActive;
} 