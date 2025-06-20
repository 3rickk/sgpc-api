package br.com.sgpc.sgpc_api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidade que representa uma tarefa dentro de um projeto.
 * 
 * Esta entidade modela as tarefas que compõem um projeto de construção,
 * incluindo cronograma, responsável, progresso, custos e controle de status.
 * Suporta workflow completo desde planejamento até conclusão.
 * 
 * Principais características:
 * - Sistema de status Kanban (A_FAZER, EM_ANDAMENTO, CONCLUIDA)
 * - Controle de cronograma planejado vs realizado
 * - Gestão de custos por categoria (mão de obra, material, equipamento)
 * - Sistema de prioridades (1-Baixa a 4-Crítica)
 * - Estimativa vs tempo real de execução
 * - Progresso percentual automático
 * - Relacionamentos com projeto, usuário responsável e criador
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * Identificador único da tarefa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título da tarefa.
     * 
     * Nome descritivo e conciso da atividade a ser executada.
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Descrição detalhada da tarefa.
     * 
     * Especificações completas do que deve ser executado,
     * incluindo requisitos e critérios de aceitação.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Status atual da tarefa.
     * 
     * Controla o fluxo de trabalho Kanban da tarefa.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.A_FAZER;

    /**
     * Data planejada para início da tarefa.
     */
    @Column(name = "start_date_planned")
    private LocalDate startDatePlanned;

    /**
     * Data planejada para conclusão da tarefa.
     */
    @Column(name = "end_date_planned")
    private LocalDate endDatePlanned;

    /**
     * Data real de início da tarefa.
     * 
     * Preenchida automaticamente quando o status muda para EM_ANDAMENTO.
     */
    @Column(name = "start_date_actual")
    private LocalDate startDateActual;

    /**
     * Data real de conclusão da tarefa.
     * 
     * Preenchida automaticamente quando o status muda para CONCLUIDA.
     */
    @Column(name = "end_date_actual")
    private LocalDate endDateActual;

    /**
     * Percentual de progresso da tarefa (0-100).
     * 
     * Indica o nível de conclusão da tarefa. Atualiza automaticamente
     * o status baseado no progresso.
     */
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    /**
     * Prioridade da tarefa.
     * 
     * Escala de 1 a 4:
     * 1 = Baixa
     * 2 = Média  
     * 3 = Alta
     * 4 = Crítica
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

    /**
     * Horas estimadas para conclusão da tarefa.
     * 
     * Planejamento inicial de tempo necessário.
     */
    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    /**
     * Horas reais trabalhadas na tarefa.
     * 
     * Tempo efetivamente gasto na execução.
     */
    @Column(name = "actual_hours")
    private Integer actualHours;

    /**
     * Observações e anotações sobre a tarefa.
     * 
     * Campo livre para registrar informações importantes,
     * atualizações de progresso e observações gerais.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Custo de mão de obra da tarefa.
     * 
     * Valor gasto com trabalho humano na execução da tarefa.
     */
    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost = BigDecimal.ZERO;

    /**
     * Custo de materiais da tarefa.
     * 
     * Valor gasto com materiais consumidos na execução.
     */
    @Column(name = "material_cost", precision = 15, scale = 2)
    private BigDecimal materialCost = BigDecimal.ZERO;

    /**
     * Custo de equipamentos da tarefa.
     * 
     * Valor gasto com aluguel ou uso de equipamentos.
     */
    @Column(name = "equipment_cost", precision = 15, scale = 2)
    private BigDecimal equipmentCost = BigDecimal.ZERO;

    /**
     * Projeto ao qual a tarefa pertence.
     * 
     * Relacionamento obrigatório indicando o projeto pai.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Usuário responsável pela execução da tarefa.
     * 
     * Pessoa designada para executar a tarefa.
     * Campo opcional, pode haver tarefas não atribuídas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    /**
     * Usuário que criou a tarefa.
     * 
     * Pessoa responsável pela criação da tarefa no sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    /**
     * Data e hora de criação da tarefa.
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
     * Inicializa campos com valores padrão.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (progressPercentage == null) {
            progressPercentage = 0;
        }
        if (priority == null) {
            priority = 1;
        }
        if (laborCost == null) {
            laborCost = BigDecimal.ZERO;
        }
        if (materialCost == null) {
            materialCost = BigDecimal.ZERO;
        }
        if (equipmentCost == null) {
            equipmentCost = BigDecimal.ZERO;
        }
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
     * Verifica se a tarefa possui usuário responsável.
     * 
     * @return true se há usuário atribuído à tarefa
     */
    public boolean isAssigned() {
        return assignedUser != null;
    }

    /**
     * Verifica se a tarefa está concluída.
     * 
     * @return true se a tarefa está no status CONCLUIDA
     */
    public boolean isCompleted() {
        return status == TaskStatus.CONCLUIDA;
    }

    /**
     * Verifica se a tarefa está em progresso.
     * 
     * @return true se a tarefa está no status EM_ANDAMENTO
     */
    public boolean isInProgress() {
        return status == TaskStatus.EM_ANDAMENTO;
    }

    /**
     * Obtém descrição textual da prioridade.
     * 
     * @return String descrição da prioridade
     */
    public String getPriorityDescription() {
        return switch (priority) {
            case 1 -> "Baixa";
            case 2 -> "Média";
            case 3 -> "Alta";
            case 4 -> "Crítica";
            default -> "Indefinida";
        };
    }

    /**
     * Calcula o custo total da tarefa.
     * 
     * Soma todos os tipos de custo (mão de obra, material, equipamento).
     * 
     * @return BigDecimal custo total da tarefa
     */
    public BigDecimal getTotalCost() {
        return laborCost.add(materialCost).add(equipmentCost);
    }

    /**
     * Atualiza os custos da tarefa.
     * 
     * Define novos valores para cada categoria de custo,
     * tratando valores nulos como zero.
     * 
     * @param laborCost novo custo de mão de obra
     * @param materialCost novo custo de materiais
     * @param equipmentCost novo custo de equipamentos
     */
    public void updateCosts(BigDecimal laborCost, BigDecimal materialCost, BigDecimal equipmentCost) {
        this.laborCost = laborCost != null ? laborCost : BigDecimal.ZERO;
        this.materialCost = materialCost != null ? materialCost : BigDecimal.ZERO;
        this.equipmentCost = equipmentCost != null ? equipmentCost : BigDecimal.ZERO;
    }

    /**
     * Verifica se a tarefa possui custos registrados.
     * 
     * @return true se o custo total é maior que zero
     */
    public boolean hasCosts() {
        return getTotalCost().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Atualiza o progresso da tarefa com lógica automática de status.
     * 
     * Além de atualizar o percentual, altera automaticamente o status
     * da tarefa baseado no progresso e atualiza datas de início/fim.
     * 
     * @param newProgress novo percentual de progresso (0-100)
     */
    public void updateProgress(Integer newProgress) {
        if (newProgress != null && newProgress >= 0 && newProgress <= 100) {
            this.progressPercentage = newProgress;
            
            // Atualizar status automaticamente baseado no progresso
            if (newProgress == 0 && this.status != TaskStatus.A_FAZER) {
                this.status = TaskStatus.A_FAZER;
                this.startDateActual = null;
                this.endDateActual = null;
            } else if (newProgress > 0 && newProgress < 100 && this.status != TaskStatus.EM_ANDAMENTO) {
                this.status = TaskStatus.EM_ANDAMENTO;
                if (this.startDateActual == null) {
                    this.startDateActual = LocalDate.now();
                }
                this.endDateActual = null;
            } else if (newProgress == 100 && this.status != TaskStatus.CONCLUIDA) {
                this.status = TaskStatus.CONCLUIDA;
                this.endDateActual = LocalDate.now();
                if (this.startDateActual == null) {
                    this.startDateActual = LocalDate.now();
                }
            }
        }
    }

    /**
     * Atualiza o status da tarefa com lógica automática de progresso.
     * 
     * Além de atualizar o status, altera automaticamente o progresso
     * da tarefa baseado no status e atualiza datas de início/fim.
     * 
     * @param newStatus novo status da tarefa
     */
    public void updateStatus(TaskStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
            
            // Atualizar progresso automaticamente baseado no status
            switch (newStatus) {
                case A_FAZER:
                    this.progressPercentage = 0;
                    this.startDateActual = null;
                    this.endDateActual = null;
                    break;
                case EM_ANDAMENTO:
                    if (this.progressPercentage == 0 || this.progressPercentage == 100) {
                        this.progressPercentage = 50; // Progresso padrão para em andamento
                    }
                    if (this.startDateActual == null) {
                        this.startDateActual = LocalDate.now();
                    }
                    this.endDateActual = null;
                    break;
                case CONCLUIDA:
                    this.progressPercentage = 100;
                    this.endDateActual = LocalDate.now();
                    if (this.startDateActual == null) {
                        this.startDateActual = LocalDate.now();
                    }
                    break;
                case BLOQUEADA:
                case CANCELADA:
                    // Manter progresso atual sem mudanças automáticas
                    break;
            }
        }
    }

    /**
     * Verifica se a tarefa está atrasada.
     * 
     * @return true se a data planejada de fim passou e a tarefa não está concluída
     */
    public boolean isOverdue() {
        return endDatePlanned != null && 
               endDatePlanned.isBefore(LocalDate.now()) && 
               !isCompleted();
    }

    /**
     * Calcula a variação de horas (planejado vs real).
     * 
     * @return Integer diferença entre horas reais e estimadas
     */
    public Integer getHoursVariance() {
        if (estimatedHours == null || actualHours == null) {
            return null;
        }
        return actualHours - estimatedHours;
    }
} 