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
 * Entidade que representa a execução de serviços em tarefas específicas.
 * 
 * Esta entidade implementa a relação many-to-many entre Task e Service,
 * permitindo que uma tarefa execute múltiplos serviços e que um serviço
 * seja executado em múltiplas tarefas com quantidades e custos específicos.
 * 
 * Funcionalidades principais:
 * - Vinculação de serviços a tarefas específicas
 * - Controle de quantidade executada por tarefa
 * - Override de custos unitários quando necessário
 * - Cálculo automático de custos totais por categoria
 * - Histórico de modificações com timestamps
 * - Notas e observações específicas da execução
 * 
 * Estrutura de custos:
 * - Mão de obra: baseado no custo do serviço ou override
 * - Material: sempre baseado no custo do serviço
 * - Equipamento: sempre baseado no custo do serviço
 * - Total: soma de todas as categorias
 * 
 * Casos de uso:
 * - Planejamento de execução de tarefas
 * - Controle de custos detalhado por serviço
 * - Relatórios de produtividade
 * - Análise de variação de custos
 * - Auditoria de execução de projetos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "task_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskService {

    /**
     * Identificador único da execução do serviço na tarefa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tarefa onde o serviço será executado.
     * 
     * Relacionamento many-to-one com lazy loading.
     * Uma tarefa pode ter múltiplos serviços associados.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * Serviço que será executado na tarefa.
     * 
     * Relacionamento many-to-one com lazy loading.
     * Um serviço pode ser executado em múltiplas tarefas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    /**
     * Quantidade do serviço a ser executada.
     * 
     * Valor padrão: 1.0
     * Precisão: 15 dígitos totais com 2 casas decimais
     * Multiplicado pelos custos unitários para cálculo total.
     */
    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    /**
     * Override do custo unitário de mão de obra.
     * 
     * Quando informado, substitui o custo padrão do serviço.
     * Usado para ajustes específicos de pricing por projeto
     * ou negociações especiais.
     */
    @Column(name = "unit_cost_override", precision = 15, scale = 2)
    private BigDecimal unitCostOverride;

    /**
     * Notas e observações específicas desta execução.
     * 
     * Campo livre para informações como:
     * - Instruções especiais de execução
     * - Justificativas para override de custos
     * - Observações de campo
     * - Dependências ou restrições
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Data e hora de criação do registro.
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
     * Define timestamps e valor padrão de quantidade.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantity == null) {
            quantity = BigDecimal.ONE;
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
     * Obtém o custo unitário efetivo de mão de obra.
     * 
     * Prioriza o override se informado, caso contrário
     * usa o custo padrão do serviço.
     * 
     * @return BigDecimal custo unitário de mão de obra
     */
    public BigDecimal getEffectiveUnitLaborCost() {
        if (unitCostOverride != null) {
            return unitCostOverride;
        }
        return service != null ? service.getUnitLaborCost() : BigDecimal.ZERO;
    }

    /**
     * Obtém o custo unitário de material.
     * 
     * Sempre baseado no custo padrão do serviço,
     * não permite override.
     * 
     * @return BigDecimal custo unitário de material
     */
    public BigDecimal getEffectiveUnitMaterialCost() {
        return service != null ? service.getUnitMaterialCost() : BigDecimal.ZERO;
    }

    /**
     * Obtém o custo unitário de equipamento.
     * 
     * Sempre baseado no custo padrão do serviço,
     * não permite override.
     * 
     * @return BigDecimal custo unitário de equipamento
     */
    public BigDecimal getEffectiveUnitEquipmentCost() {
        return service != null ? service.getUnitEquipmentCost() : BigDecimal.ZERO;
    }

    /**
     * Calcula o custo total de mão de obra.
     * 
     * Multiplica o custo unitário efetivo pela quantidade.
     * 
     * @return BigDecimal custo total de mão de obra
     */
    public BigDecimal getTotalLaborCost() {
        return getEffectiveUnitLaborCost().multiply(quantity);
    }

    /**
     * Calcula o custo total de material.
     * 
     * Multiplica o custo unitário de material pela quantidade.
     * 
     * @return BigDecimal custo total de material
     */
    public BigDecimal getTotalMaterialCost() {
        return getEffectiveUnitMaterialCost().multiply(quantity);
    }

    /**
     * Calcula o custo total de equipamento.
     * 
     * Multiplica o custo unitário de equipamento pela quantidade.
     * 
     * @return BigDecimal custo total de equipamento
     */
    public BigDecimal getTotalEquipmentCost() {
        return getEffectiveUnitEquipmentCost().multiply(quantity);
    }

    /**
     * Calcula o custo total da execução do serviço.
     * 
     * Soma os custos de mão de obra, material e equipamento.
     * 
     * @return BigDecimal custo total da execução
     */
    public BigDecimal getTotalCost() {
        return getTotalLaborCost().add(getTotalMaterialCost()).add(getTotalEquipmentCost());
    }

    /**
     * Obtém o nome do serviço executado.
     * 
     * @return String nome do serviço ou null se não definido
     */
    public String getServiceName() {
        return service != null ? service.getName() : null;
    }

    /**
     * Obtém a unidade de medida do serviço.
     * 
     * @return String unidade de medida ou null se não definida
     */
    public String getServiceUnitOfMeasurement() {
        return service != null ? service.getUnitOfMeasurement() : null;
    }

    /**
     * Obtém a descrição do serviço executado.
     * 
     * @return String descrição do serviço ou null se não definida
     */
    public String getServiceDescription() {
        return service != null ? service.getDescription() : null;
    }

    /**
     * Verifica se foi aplicado override de custo unitário.
     * 
     * @return boolean true se há override de custo
     */
    public boolean hasCustomUnitCost() {
        return unitCostOverride != null;
    }
} 