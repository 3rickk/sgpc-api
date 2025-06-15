package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    
    private Long id;
    private String name;
    private String description;
    private String unitOfMeasurement;
    private BigDecimal unitLaborCost;
    private BigDecimal unitMaterialCost;
    private BigDecimal unitEquipmentCost;
    private BigDecimal totalUnitCost;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 