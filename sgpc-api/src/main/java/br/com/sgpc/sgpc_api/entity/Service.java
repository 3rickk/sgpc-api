package br.com.sgpc.sgpc_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_of_measurement", nullable = false, length = 50)
    private String unitOfMeasurement;

    @Column(name = "unit_labor_cost", precision = 15, scale = 2)
    private BigDecimal unitLaborCost = BigDecimal.ZERO;

    @Column(name = "unit_material_cost", precision = 15, scale = 2)
    private BigDecimal unitMaterialCost = BigDecimal.ZERO;

    @Column(name = "unit_equipment_cost", precision = 15, scale = 2)
    private BigDecimal unitEquipmentCost = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (unitLaborCost == null) {
            unitLaborCost = BigDecimal.ZERO;
        }
        if (unitMaterialCost == null) {
            unitMaterialCost = BigDecimal.ZERO;
        }
        if (unitEquipmentCost == null) {
            unitEquipmentCost = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public BigDecimal getTotalUnitCost() {
        return unitLaborCost.add(unitMaterialCost).add(unitEquipmentCost);
    }

    public boolean isServiceActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public BigDecimal calculateTotalCost(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return getTotalUnitCost().multiply(quantity);
    }

    public BigDecimal calculateLaborCost(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return unitLaborCost.multiply(quantity);
    }

    public BigDecimal calculateMaterialCost(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return unitMaterialCost.multiply(quantity);
    }

    public BigDecimal calculateEquipmentCost(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return unitEquipmentCost.multiply(quantity);
    }
} 