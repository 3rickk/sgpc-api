package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de serviços do sistema.
 * 
 * Esta classe representa um serviço com seus custos unitários
 * de mão de obra, materiais e equipamentos para uso em projetos
 * de construção.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de um serviço incluindo custos unitários")
public class ServiceDto {
    
    @Schema(description = "ID único do serviço", example = "1")
    private Long id;
    
    @Schema(description = "Nome do serviço", example = "Alvenaria de tijolos", maxLength = 255)
    private String name;
    
    @Schema(description = "Descrição detalhada do serviço", example = "Execução de alvenaria com tijolos cerâmicos", maxLength = 1000)
    private String description;
    
    @Schema(description = "Unidade de medida do serviço", example = "m²", maxLength = 50)
    private String unitOfMeasurement;
    
    @Schema(description = "Custo unitário de mão de obra", example = "15.50")
    private BigDecimal unitLaborCost;
    
    @Schema(description = "Custo unitário de materiais", example = "12.30")
    private BigDecimal unitMaterialCost;
    
    @Schema(description = "Custo unitário de equipamentos", example = "3.20")
    private BigDecimal unitEquipmentCost;
    
    @Schema(description = "Custo unitário total (soma de todos os custos)", example = "31.00")
    private BigDecimal totalUnitCost;
    
    @Schema(description = "Indica se o serviço está ativo", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Data de criação do serviço", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;
} 