package br.com.sgpc.sgpc_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_cost_override", precision = 15, scale = 2)
    private BigDecimal unitCostOverride; // Sobrescreve o custo unitário padrão do serviço

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
        if (quantity == null) {
            quantity = BigDecimal.ONE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência para cálculos de custos
    public BigDecimal getEffectiveUnitLaborCost() {
        if (unitCostOverride != null) {
            return unitCostOverride;
        }
        return service != null ? service.getUnitLaborCost() : BigDecimal.ZERO;
    }

    public BigDecimal getEffectiveUnitMaterialCost() {
        return service != null ? service.getUnitMaterialCost() : BigDecimal.ZERO;
    }

    public BigDecimal getEffectiveUnitEquipmentCost() {
        return service != null ? service.getUnitEquipmentCost() : BigDecimal.ZERO;
    }

    public BigDecimal getTotalLaborCost() {
        return getEffectiveUnitLaborCost().multiply(quantity);
    }

    public BigDecimal getTotalMaterialCost() {
        return getEffectiveUnitMaterialCost().multiply(quantity);
    }

    public BigDecimal getTotalEquipmentCost() {
        return getEffectiveUnitEquipmentCost().multiply(quantity);
    }

    public BigDecimal getTotalCost() {
        return getTotalLaborCost().add(getTotalMaterialCost()).add(getTotalEquipmentCost());
    }

    // Métodos de conveniência para informações do serviço
    public String getServiceName() {
        return service != null ? service.getName() : null;
    }

    public String getServiceUnitOfMeasurement() {
        return service != null ? service.getUnitOfMeasurement() : null;
    }

    public String getServiceDescription() {
        return service != null ? service.getDescription() : null;
    }

    public boolean hasCustomUnitCost() {
        return unitCostOverride != null;
    }
} 