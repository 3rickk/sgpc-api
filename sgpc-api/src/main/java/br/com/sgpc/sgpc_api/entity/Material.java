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
 * Entidade que representa um material de construção.
 * 
 * Esta entidade modela os materiais utilizados em projetos de construção,
 * incluindo controle de estoque, preços e informações de fornecimento.
 * Suporta operações de entrada e saída de estoque com validações.
 * 
 * Principais características:
 * - Controle de estoque atual e mínimo
 * - Alertas de baixo estoque
 * - Histórico de preços e fornecedores
 * - Operações seguras de movimentação de estoque
 * - Status ativo/inativo para controle
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    /**
     * Identificador único do material.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do material.
     * 
     * Identificação clara e única do material de construção.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Descrição detalhada do material.
     * 
     * Especificações técnicas, características e outras
     * informações relevantes sobre o material.
     */
    @Column(name = "description", length = 5000)
    private String description;

    /**
     * Unidade de medida do material.
     * 
     * Define como o material é quantificado no estoque.
     * Exemplos: "unidade", "kg", "m²", "m³", "saco"
     */
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    /**
     * Preço unitário do material.
     * 
     * Valor por unidade de medida para cálculos de custo.
     */
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /**
     * Nome do fornecedor principal.
     * 
     * Empresa ou pessoa responsável pelo fornecimento do material.
     */
    @Column(name = "supplier")
    private String supplier;

    /**
     * Quantidade atual em estoque.
     * 
     * Quantidade disponível do material no momento,
     * atualizada automaticamente nas movimentações.
     */
    @Column(name = "current_stock", precision = 10, scale = 3, nullable = false)
    private BigDecimal currentStock = BigDecimal.ZERO;

    /**
     * Estoque mínimo para alertas.
     * 
     * Quantidade limite abaixo da qual deve ser gerado
     * alerta de baixo estoque.
     */
    @Column(name = "minimum_stock", precision = 10, scale = 3, nullable = false)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    /**
     * Indica se o material está ativo no sistema.
     * 
     * Materiais inativos não podem ser utilizados em novos projetos,
     * mas são mantidos para preservar histórico.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Data e hora de criação do material.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Método executado antes da persistência.
     * 
     * Inicializa campos de timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Método executado antes da atualização.
     * 
     * Atualiza timestamp de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se o estoque atual está abaixo do mínimo.
     * 
     * @return true se o estoque está abaixo do mínimo configurado
     */
    public boolean isStockBelowMinimum() {
        return currentStock.compareTo(minimumStock) < 0;
    }

    /**
     * Adiciona quantidade ao estoque.
     * 
     * Operação segura que só permite adições de valores positivos.
     * 
     * @param quantity quantidade a ser adicionada (deve ser positiva)
     */
    public void addStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) > 0) {
            this.currentStock = this.currentStock.add(quantity);
        }
    }

    /**
     * Remove quantidade do estoque.
     * 
     * Operação segura que valida se há estoque suficiente
     * antes de realizar a remoção.
     * 
     * @param quantity quantidade a ser removida (deve ser positiva)
     * @throws RuntimeException se não houver estoque suficiente
     */
    public void removeStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) > 0 && 
            this.currentStock.compareTo(quantity) >= 0) {
            this.currentStock = this.currentStock.subtract(quantity);
        } else {
            throw new RuntimeException("Estoque insuficiente para " + this.name);
        }
    }

    /**
     * Calcula o valor total do estoque atual.
     * 
     * @return BigDecimal valor total (estoque atual × preço unitário)
     */
    public BigDecimal getTotalStockValue() {
        return currentStock.multiply(unitPrice);
    }

    /**
     * Verifica se o material está ativo e pode ser utilizado.
     * 
     * @return true se o material está ativo
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(isActive);
    }
} 