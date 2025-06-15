package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockReportDto {
    private Long materialId;
    private String materialName;
    private String category;
    private String unit;
    private Integer currentQuantity;
    private Integer minimumQuantity;
    private Boolean lowStock;
    private BigDecimal unitCost;
    private BigDecimal totalValue;
    private LocalDate lastUpdated;
    private String supplier;

    // Construtores
    public StockReportDto() {}

    public StockReportDto(Long materialId, String materialName, String category, String unit,
                         Integer currentQuantity, Integer minimumQuantity, Boolean lowStock,
                         BigDecimal unitCost, BigDecimal totalValue, LocalDate lastUpdated, String supplier) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.category = category;
        this.unit = unit;
        this.currentQuantity = currentQuantity;
        this.minimumQuantity = minimumQuantity;
        this.lowStock = lowStock;
        this.unitCost = unitCost;
        this.totalValue = totalValue;
        this.lastUpdated = lastUpdated;
        this.supplier = supplier;
    }

    // Getters e Setters
    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Integer currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public Boolean getLowStock() {
        return lowStock;
    }

    public void setLowStock(Boolean lowStock) {
        this.lowStock = lowStock;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
} 