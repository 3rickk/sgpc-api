package br.com.sgpc.sgpc_api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um projeto de construção.
 * 
 * Esta entidade modela os projetos de construção civil no sistema SGPC,
 * incluindo informações como cronograma, orçamento, equipe e status.
 * Cada projeto pode ter múltiplas tarefas e vários membros na equipe.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    /**
     * Identificador único do projeto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do projeto.
     * 
     * Título descritivo e único do projeto de construção.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Descrição detalhada do projeto.
     * 
     * Informações completas sobre o escopo, objetivos e características do projeto.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Data planejada para início do projeto.
     */
    @Column(name = "start_date_planned")
    private LocalDate startDatePlanned;

    /**
     * Data planejada para conclusão do projeto.
     */
    @Column(name = "end_date_planned")
    private LocalDate endDatePlanned;

    /**
     * Data real de início do projeto.
     * 
     * Preenchida quando o projeto é efetivamente iniciado.
     */
    @Column(name = "start_date_actual")
    private LocalDate startDateActual;

    /**
     * Data real de conclusão do projeto.
     * 
     * Preenchida quando o projeto é concluído.
     */
    @Column(name = "end_date_actual")
    private LocalDate endDateActual;

    /**
     * Orçamento total do projeto.
     * 
     * Valor total planejado para execução do projeto.
     */
    @Column(name = "total_budget", precision = 15, scale = 2)
    private BigDecimal totalBudget;

    /**
     * Nome do cliente do projeto.
     * 
     * Cliente/empresa para quem o projeto está sendo executado.
     */
    @Column(name = "client", length = 255)
    private String client;

    /**
     * Status atual do projeto.
     * 
     * Indica a fase atual do projeto (planejamento, em andamento, concluído, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.PLANEJAMENTO;

    /**
     * Custo realizado até o momento.
     * 
     * Valor já gasto na execução do projeto, calculado automaticamente
     * com base nas tarefas e materiais utilizados.
     */
    @Column(name = "realized_cost", precision = 15, scale = 2)
    private BigDecimal realizedCost = BigDecimal.ZERO;

    /**
     * Percentual de progresso do projeto.
     * 
     * Valor entre 0 e 100 indicando o progresso geral do projeto,
     * calculado com base no progresso das tarefas.
     */
    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    /**
     * Data de criação do registro.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data da última atualização do registro.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Membros da equipe do projeto.
     * 
     * Conjunto de usuários que fazem parte da equipe responsável
     * pela execução do projeto.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> teamMembers = new HashSet<>();

    /**
     * Método executado antes da persistência da entidade.
     * 
     * Inicializa os campos de timestamps e valores padrão.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (realizedCost == null) {
            realizedCost = BigDecimal.ZERO;
        }
        if (progressPercentage == null) {
            progressPercentage = BigDecimal.ZERO;
        }
    }

    /**
     * Método executado antes da atualização da entidade.
     * 
     * Atualiza o timestamp de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência para gerenciar equipe

    /**
     * Adiciona um usuário à equipe do projeto.
     * 
     * @param user usuário a ser adicionado à equipe
     */
    public void addTeamMember(User user) {
        teamMembers.add(user);
    }

    /**
     * Remove um usuário da equipe do projeto.
     * 
     * @param user usuário a ser removido da equipe
     */
    public void removeTeamMember(User user) {
        teamMembers.remove(user);
    }

    /**
     * Verifica se um usuário faz parte da equipe do projeto.
     * 
     * @param user usuário a ser verificado
     * @return true se o usuário faz parte da equipe, false caso contrário
     */
    public boolean hasTeamMember(User user) {
        return teamMembers.contains(user);
    }

    /**
     * Obtém o tamanho da equipe do projeto.
     * 
     * @return número de membros na equipe
     */
    public int getTeamSize() {
        return teamMembers.size();
    }

    // Métodos para gestão de orçamento

    /**
     * Calcula a variação do orçamento.
     * 
     * @return diferença entre o orçamento planejado e o custo realizado.
     *         Valor positivo indica que está dentro do orçamento,
     *         valor negativo indica estouro de orçamento.
     */
    public BigDecimal getBudgetVariance() {
        if (totalBudget == null) {
            return realizedCost.negate();
        }
        return totalBudget.subtract(realizedCost);
    }

    /**
     * Verifica se o projeto está acima do orçamento.
     * 
     * @return true se o custo realizado é maior que o orçamento total
     */
    public boolean isOverBudget() {
        return getBudgetVariance().compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Calcula o percentual de uso do orçamento.
     * 
     * @return percentual do orçamento já utilizado (0-100+)
     */
    public BigDecimal getBudgetUsagePercentage() {
        if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return realizedCost.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Atualiza o custo realizado do projeto.
     * 
     * @param newRealizedCost novo valor do custo realizado
     */
    public void updateRealizedCost(BigDecimal newRealizedCost) {
        this.realizedCost = newRealizedCost != null ? newRealizedCost : BigDecimal.ZERO;
    }

    /**
     * Atualiza o progresso do projeto.
     * 
     * @param newProgress novo percentual de progresso (0-100)
     */
    public void updateProgress(BigDecimal newProgress) {
        if (newProgress != null && newProgress.compareTo(BigDecimal.ZERO) >= 0 && newProgress.compareTo(BigDecimal.valueOf(100)) <= 0) {
            this.progressPercentage = newProgress;
        }
    }

    // Métodos de conveniência para status do projeto

    /**
     * Verifica se o projeto está ativo.
     * 
     * @return true se o projeto está em andamento ou planejamento
     */
    public boolean isActive() {
        return status == ProjectStatus.EM_ANDAMENTO || status == ProjectStatus.PLANEJAMENTO;
    }

    /**
     * Verifica se o projeto está concluído.
     * 
     * @return true se o projeto foi concluído
     */
    public boolean isCompleted() {
        return status == ProjectStatus.CONCLUIDO;
    }

    /**
     * Verifica se o projeto está pausado.
     * 
     * @return true se o projeto foi pausado
     */
    public boolean isPaused() {
        return status == ProjectStatus.PAUSADO;
    }

    /**
     * Verifica se o projeto foi cancelado.
     * 
     * @return true se o projeto foi cancelado
     */
    public boolean isCancelled() {
        return status == ProjectStatus.CANCELADO;
    }
} 