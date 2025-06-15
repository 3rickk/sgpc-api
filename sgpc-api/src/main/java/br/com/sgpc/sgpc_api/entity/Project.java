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

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date_planned")
    private LocalDate startDatePlanned;

    @Column(name = "end_date_planned")
    private LocalDate endDatePlanned;

    @Column(name = "start_date_actual")
    private LocalDate startDateActual;

    @Column(name = "end_date_actual")
    private LocalDate endDateActual;

    @Column(name = "total_budget", precision = 15, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "client", length = 255)
    private String client;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.PLANEJAMENTO;

    // Novos campos de orçamento e progresso
    @Column(name = "realized_cost", precision = 15, scale = 2)
    private BigDecimal realizedCost = BigDecimal.ZERO;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> teamMembers = new HashSet<>();

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

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência para gerenciar equipe
    public void addTeamMember(User user) {
        teamMembers.add(user);
    }

    public void removeTeamMember(User user) {
        teamMembers.remove(user);
    }

    public boolean hasTeamMember(User user) {
        return teamMembers.contains(user);
    }

    public int getTeamSize() {
        return teamMembers.size();
    }

    // Métodos para gestão de orçamento
    public BigDecimal getBudgetVariance() {
        if (totalBudget == null) {
            return realizedCost.negate();
        }
        return totalBudget.subtract(realizedCost);
    }

    public boolean isOverBudget() {
        return getBudgetVariance().compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getBudgetUsagePercentage() {
        if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return realizedCost.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 2, java.math.RoundingMode.HALF_UP);
    }

    public void updateRealizedCost(BigDecimal newRealizedCost) {
        this.realizedCost = newRealizedCost != null ? newRealizedCost : BigDecimal.ZERO;
    }

    public void updateProgress(BigDecimal newProgress) {
        if (newProgress != null && newProgress.compareTo(BigDecimal.ZERO) >= 0 && newProgress.compareTo(BigDecimal.valueOf(100)) <= 0) {
            this.progressPercentage = newProgress;
        }
    }

    // Métodos de conveniência para status do projeto
    public boolean isActive() {
        return status == ProjectStatus.EM_ANDAMENTO || status == ProjectStatus.PLANEJAMENTO;
    }

    public boolean isCompleted() {
        return status == ProjectStatus.CONCLUIDO;
    }

    public boolean isSuspended() {
        return status == ProjectStatus.SUSPENSO;
    }

    public boolean isCancelled() {
        return status == ProjectStatus.CANCELADO;
    }
} 