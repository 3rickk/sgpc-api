package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskServiceDto {
    
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private String unitOfMeasurement;
    private BigDecimal quantity;
    private BigDecimal unitCostOverride;
    private BigDecimal unitLaborCost;
    private BigDecimal unitMaterialCost;
    private BigDecimal unitEquipmentCost;
    private BigDecimal totalLaborCost;
    private BigDecimal totalMaterialCost;
    private BigDecimal totalEquipmentCost;
    private BigDecimal totalCost;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 