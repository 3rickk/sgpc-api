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

/**
 * Entidade que representa itens individuais de uma solicitação de materiais.
 * 
 * Esta entidade detalha cada material específico solicitado dentro de uma
 * MaterialRequest, incluindo quantidade, preços e observações particulares.
 * 
 * Funcionalidades:
 * - Vinculação a material específico do catálogo
 * - Controle de quantidade solicitada
 * - Preço unitário (copiado do material se não informado)
 * - Cálculo automático do preço total
 * - Observações específicas por item
 * - Auditoria com timestamps
 * 
 * Relacionamentos:
 * - ManyToOne com MaterialRequest (solicitação pai)
 * - ManyToOne com Material (material solicitado)
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "material_request_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestItem {

    /**
     * Identificador único do item na solicitação.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Solicitação à qual este item pertence.
     * 
     * Relacionamento many-to-one com lazy loading.
     * Múltiplos itens podem pertencer a uma solicitação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false)
    private MaterialRequest materialRequest;

    /**
     * Material solicitado do catálogo.
     * 
     * Relacionamento many-to-one com lazy loading.
     * Referencia um material específico do estoque.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * Quantidade solicitada do material.
     * 
     * Precisão de 10 dígitos com 3 casas decimais para
     * suportar diferentes unidades de medida.
     */
    @Column(name = "quantity", precision = 10, scale = 3, nullable = false)
    private BigDecimal quantity;

    /**
     * Preço unitário do material na data da solicitação.
     * 
     * Se não informado, é copiado automaticamente do
     * material na criação (via @PrePersist).
     * Precisão de 10 dígitos com 2 casas decimais.
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Observações específicas para este item.
     * 
     * Campo livre para informações como preferência
     * de marca, especificações técnicas, urgência.
     */
    @Column(name = "observations", length = 1000)
    private String observations;

    /**
     * Data e hora de criação do item.
     * 
     * Preenchida automaticamente pelo Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização.
     * 
     * Atualizada automaticamente pelo Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Callback executado antes da persistência inicial.
     * 
     * Define timestamps automaticamente e copia o preço
     * unitário do material se não foi informado.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Copia o preço unitário do material se não informado
        if (unitPrice == null && material != null) {
            unitPrice = material.getUnitPrice();
        }
    }

    /**
     * Callback executado antes de cada atualização.
     * 
     * Atualiza o timestamp de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula o preço total do item.
     * 
     * Multiplica quantidade pelo preço unitário.
     * Retorna zero se algum dos valores for nulo.
     * 
     * @return BigDecimal preço total (quantidade * preço unitário)
     */
    public BigDecimal getTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(quantity);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Verifica se o item tem preço válido para cálculos.
     * 
     * @return true se tanto quantidade quanto preço unitário estão definidos
     */
    public boolean hasValidPrice() {
        return unitPrice != null && quantity != null && 
               unitPrice.compareTo(BigDecimal.ZERO) > 0 && 
               quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Construtor de conveniência para criação de itens.
     * 
     * @param materialRequest solicitação pai
     * @param material material solicitado
     * @param quantity quantidade solicitada
     * @param observations observações específicas
     */
    public MaterialRequestItem(MaterialRequest materialRequest, Material material, 
                             BigDecimal quantity, String observations) {
        this.materialRequest = materialRequest;
        this.material = material;
        this.quantity = quantity;
        this.observations = observations;
    }
} 