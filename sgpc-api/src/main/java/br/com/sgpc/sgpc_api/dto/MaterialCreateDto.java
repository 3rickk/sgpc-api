package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de novos materiais no sistema.
 * 
 * Esta classe contém todos os dados necessários para criar um novo
 * material no sistema SGPC, incluindo validações de entrada para
 * garantir a integridade e qualidade dos dados.
 * 
 * Funcionalidades de validação:
 * - Nome obrigatório e único
 * - Categoria para organização
 * - Unidade de medida padronizada
 * - Custos unitários positivos
 * - Controle de estoque opcional
 * - Informações de fornecedor
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um novo material")
public class MaterialCreateDto {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do material", 
            example = "Cimento Portland CP-II",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Schema(description = "Descrição detalhada do material", 
            example = "Cimento Portland Composto com escória, ideal para obras em geral",
            maxLength = 1000)
    private String description;
    
    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    @Schema(description = "Categoria do material", 
            example = "Cimento",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;
    
    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres")
    @Schema(description = "Unidade de medida", 
            example = "saco",
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;
    
    @NotNull(message = "Custo unitário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Custo unitário deve ser maior que zero")
    @Schema(description = "Custo unitário do material", 
            example = "28.50",
            minimum = "0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitCost;
    
    @Min(value = 0, message = "Estoque atual deve ser zero ou positivo")
    @Schema(description = "Quantidade atual em estoque", 
            example = "100",
            minimum = "0",
            defaultValue = "0")
    private Integer currentStock = 0;
    
    @Min(value = 0, message = "Estoque mínimo deve ser zero ou positivo")
    @Schema(description = "Quantidade mínima recomendada em estoque", 
            example = "20",
            minimum = "0",
            defaultValue = "0")
    private Integer minimumStock = 0;
    
    @Size(max = 255, message = "Nome do fornecedor deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do fornecedor principal", 
            example = "Cimentos Brasil Ltda",
            maxLength = 255)
    private String supplier;
} 