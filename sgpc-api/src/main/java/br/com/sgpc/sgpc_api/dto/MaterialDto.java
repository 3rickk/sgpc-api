package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {

    private Long id;
    private String name;
    private String description;
    private String unitOfMeasure;
    private BigDecimal unitPrice;
    private String supplier;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private Boolean isActive;
    private Boolean isBelowMinimum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 