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
public class ServiceCreateDto {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String name;
    
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;
    
    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(max = 50, message = "Unidade de medida deve ter no máximo 50 caracteres")
    private String unitOfMeasurement;
    
    @NotNull(message = "Custo unitário de mão de obra é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de mão de obra deve ser positivo")
    private BigDecimal unitLaborCost;
    
    @NotNull(message = "Custo unitário de materiais é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de materiais deve ser positivo")
    private BigDecimal unitMaterialCost;
    
    @NotNull(message = "Custo unitário de equipamentos é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Custo de equipamentos deve ser positivo")
    private BigDecimal unitEquipmentCost;
    
    private Boolean isActive = true;
} 