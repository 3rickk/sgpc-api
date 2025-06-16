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

/**
 * Entidade que representa um serviço no sistema.
 * 
 * Um serviço define uma atividade específica que pode ser executada
 * em projetos de construção, com custos unitários definidos para
 * mão de obra, materiais e equipamentos. Os serviços são utilizados
 * para compor o orçamento e controlar os custos das tarefas.
 * 
 * Principais características:
 * - Nome único no sistema
 * - Descrição detalhada da atividade
 * - Unidade de medida (m², m³, unidade, etc.)
 * - Custos unitários separados por categoria
 * - Status ativo/inativo para controle
 * - Timestamps de criação e atualização
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    /**
     * Identificador único do serviço.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do serviço.
     * 
     * Deve ser único no sistema para evitar duplicações.
     * Exemplo: "Alvenaria de tijolos", "Pintura interna"
     */
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    /**
     * Descrição detalhada do serviço.
     * 
     * Inclui especificações técnicas, materiais utilizados
     * e outras informações relevantes para a execução.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Unidade de medida do serviço.
     * 
     * Define como o serviço é quantificado para cálculo de custos.
     * Exemplos: "m²", "m³", "m", "unidade", "kg"
     */
    @Column(name = "unit_of_measurement", nullable = false, length = 50)
    private String unitOfMeasurement;

    /**
     * Custo unitário de mão de obra.
     * 
     * Valor cobrado por unidade de medida para a mão de obra
     * necessária para executar este serviço.
     */
    @Column(name = "unit_labor_cost", precision = 15, scale = 2)
    private BigDecimal unitLaborCost = BigDecimal.ZERO;

    /**
     * Custo unitário de materiais.
     * 
     * Valor dos materiais necessários por unidade de medida
     * para executar este serviço.
     */
    @Column(name = "unit_material_cost", precision = 15, scale = 2)
    private BigDecimal unitMaterialCost = BigDecimal.ZERO;

    /**
     * Custo unitário de equipamentos.
     * 
     * Valor do uso de equipamentos por unidade de medida
     * para executar este serviço.
     */
    @Column(name = "unit_equipment_cost", precision = 15, scale = 2)
    private BigDecimal unitEquipmentCost = BigDecimal.ZERO;

    /**
     * Indica se o serviço está ativo no sistema.
     * 
     * Serviços inativos não podem ser utilizados em novas tarefas,
     * mas são mantidos para preservar histórico de projetos existentes.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Data e hora de criação do serviço.
     * 
     * Preenchida automaticamente pelo Hibernate na criação.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização.
     * 
     * Atualizada automaticamente pelo Hibernate a cada modificação.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calcula o custo unitário total do serviço.
     * 
     * Soma os custos de mão de obra, materiais e equipamentos
     * para obter o custo total por unidade de medida.
     * 
     * @return BigDecimal custo unitário total
     */
    public BigDecimal getTotalUnitCost() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (unitLaborCost != null) {
            total = total.add(unitLaborCost);
        }
        
        if (unitMaterialCost != null) {
            total = total.add(unitMaterialCost);
        }
        
        if (unitEquipmentCost != null) {
            total = total.add(unitEquipmentCost);
        }
        
        return total;
    }

    /**
     * Operações a serem executadas antes da persistência.
     * 
     * Garante que campos obrigatórios tenham valores padrão.
     */
    @PrePersist
    protected void onCreate() {
        if (unitLaborCost == null) {
            unitLaborCost = BigDecimal.ZERO;
        }
        if (unitMaterialCost == null) {
            unitMaterialCost = BigDecimal.ZERO;
        }
        if (unitEquipmentCost == null) {
            unitEquipmentCost = BigDecimal.ZERO;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Operações a serem executadas antes da atualização.
     * 
     * Garante consistência dos dados ao atualizar.
     */
    @PreUpdate
    protected void onUpdate() {
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

    // Métodos de conveniência
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