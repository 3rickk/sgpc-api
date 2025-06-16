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
 * DTO para criação de novos serviços.
 * 
 * Esta classe contém os dados necessários para criar um novo
 * serviço no sistema, incluindo validações de entrada para
 * garantir a integridade dos dados.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um novo serviço")
public class ServiceCreateDto {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do serviço", example = "Alvenaria de tijolos", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Schema(description = "Descrição detalhada do serviço", example = "Execução de alvenaria com tijolos cerâmicos furados", maxLength = 1000)
    private String description;
    
    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(max = 50, message = "Unidade de medida deve ter no máximo 50 caracteres")
    @Schema(description = "Unidade de medida do serviço", example = "m²", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String unitOfMeasurement;
    
    @NotNull(message = "Custo unitário de mão de obra é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de mão de obra deve ser positivo")
    @Schema(description = "Custo unitário de mão de obra", example = "15.50", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitLaborCost;
    
    @NotNull(message = "Custo unitário de materiais é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de materiais deve ser positivo")
    @Schema(description = "Custo unitário de materiais", example = "12.30", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitMaterialCost;
    
    @NotNull(message = "Custo unitário de equipamentos é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de equipamentos deve ser positivo")
    @Schema(description = "Custo unitário de equipamentos", example = "3.20", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitEquipmentCost;
    
    @Schema(description = "Indica se o serviço deve ser criado como ativo", example = "true", defaultValue = "true")
    private Boolean isActive = true;
} 