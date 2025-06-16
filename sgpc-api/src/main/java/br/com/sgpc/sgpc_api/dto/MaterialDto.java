package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de materiais de construção.
 * 
 * Esta classe representa um material utilizado em projetos de construção,
 * incluindo informações de estoque, preços e controle de fornecimento.
 * É utilizada para exibição e transferência de dados de materiais.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de um material de construção")
public class MaterialDto {

    @Schema(description = "ID único do material", example = "1")
    private Long id;
    
    @Schema(description = "Nome do material", example = "Cimento Portland", maxLength = 255)
    private String name;
    
    @Schema(description = "Descrição detalhada do material", example = "Cimento Portland CP II-F-32 - Saco 50kg")
    private String description;
    
    @Schema(description = "Unidade de medida", example = "saco", maxLength = 50)
    private String unitOfMeasure;
    
    @Schema(description = "Preço unitário do material", example = "25.50")
    private BigDecimal unitPrice;
    
    @Schema(description = "Nome do fornecedor", example = "Fornecedor ABC Materiais", maxLength = 255)
    private String supplier;
    
    @Schema(description = "Quantidade atual em estoque", example = "150.00")
    private BigDecimal currentStock;
    
    @Schema(description = "Estoque mínimo para alerta", example = "20.00")
    private BigDecimal minimumStock;
    
    @Schema(description = "Indica se o material está ativo", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Indica se o estoque está abaixo do mínimo", example = "false")
    private Boolean isBelowMinimum;
    
    @Schema(description = "Data de criação do material", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;
} 