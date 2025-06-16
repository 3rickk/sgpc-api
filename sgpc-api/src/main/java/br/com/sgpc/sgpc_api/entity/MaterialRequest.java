package br.com.sgpc.sgpc_api.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.sgpc.sgpc_api.enums.RequestStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa solicitações de materiais no sistema.
 * 
 * Esta entidade gerencia o workflow completo de solicitação de materiais
 * para projetos, incluindo:
 * - Criação da solicitação por usuários do projeto
 * - Processo de aprovação/rejeição por gestores
 * - Controle de status e prazos
 * - Rastreabilidade completa do processo
 * - Gestão de itens solicitados
 * 
 * Workflow de solicitação:
 * 1. Usuário cria solicitação para um projeto
 * 2. Sistema valida disponibilidade de materiais
 * 3. Solicitação fica PENDENTE aguardando aprovação
 * 4. Gestor/Admin aprova ou rejeita com justificativa
 * 5. Se aprovada, materiais são reservados/entregues
 * 
 * Relacionamentos:
 * - ManyToOne com Project (projeto solicitante)
 * - ManyToOne com User (solicitante e aprovador)
 * - OneToMany com MaterialRequestItem (itens solicitados)
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "material_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequest {

    /**
     * Identificador único da solicitação.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Projeto para o qual os materiais são solicitados.
     * 
     * Relacionamento many-to-one com lazy loading.
     * Um projeto pode ter múltiplas solicitações de materiais.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Usuário que criou a solicitação.
     * 
     * Relacionamento many-to-one para rastreabilidade.
     * Geralmente um membro da equipe do projeto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * Data em que a solicitação foi criada.
     * 
     * Preenchida automaticamente na criação se não informada.
     */
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    /**
     * Data em que os materiais são necessários.
     * 
     * Campo opcional para planejamento de entregas.
     */
    @Column(name = "needed_date")
    private LocalDate neededDate;

    /**
     * Status atual da solicitação.
     * 
     * Valores possíveis: PENDENTE, APROVADA, REJEITADA
     * Valor padrão: PENDENTE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDENTE;

    /**
     * Motivo da rejeição da solicitação.
     * 
     * Campo obrigatório quando status é REJEITADA.
     * Usado para feedback ao solicitante.
     */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /**
     * Usuário que aprovou ou rejeitou a solicitação.
     * 
     * Relacionamento many-to-one para auditoria.
     * Geralmente um usuário com role MANAGER ou ADMIN.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    /**
     * Data e hora da aprovação/rejeição.
     * 
     * Preenchida automaticamente nos métodos approve() e reject().
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Observações gerais sobre a solicitação.
     * 
     * Campo livre para informações adicionais como
     * urgência, justificativas, instruções especiais.
     */
    @Column(name = "observations", length = 2000)
    private String observations;

    /**
     * Data e hora de criação da solicitação.
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
     * Lista de itens solicitados.
     * 
     * Relacionamento one-to-many com cascade ALL e orphan removal.
     * Gerencia automaticamente a vida útil dos itens.
     */
    @OneToMany(mappedBy = "materialRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MaterialRequestItem> items = new ArrayList<>();

    /**
     * Callback executado antes da persistência inicial.
     * 
     * Define timestamps e data da solicitação se não informada.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestDate == null) {
            requestDate = LocalDate.now();
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
     * Adiciona um item à solicitação.
     * 
     * Método de conveniência que mantém a sincronização
     * bidirecional do relacionamento.
     * 
     * @param item item a ser adicionado
     */
    public void addItem(MaterialRequestItem item) {
        items.add(item);
        item.setMaterialRequest(this);
    }

    /**
     * Remove um item da solicitação.
     * 
     * Método de conveniência que mantém a sincronização
     * bidirecional do relacionamento.
     * 
     * @param item item a ser removido
     */
    public void removeItem(MaterialRequestItem item) {
        items.remove(item);
        item.setMaterialRequest(null);
    }

    /**
     * Aprova a solicitação.
     * 
     * Atualiza o status para APROVADA, define o aprovador
     * e registra o timestamp da aprovação. Limpa qualquer
     * motivo de rejeição anterior.
     * 
     * @param approver usuário que está aprovando
     */
    public void approve(User approver) {
        this.status = RequestStatus.APROVADA;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    /**
     * Rejeita a solicitação com justificativa.
     * 
     * Atualiza o status para REJEITADA, define o aprovador,
     * registra o timestamp e o motivo da rejeição.
     * 
     * @param approver usuário que está rejeitando
     * @param reason motivo da rejeição
     */
    public void reject(User approver, String reason) {
        this.status = RequestStatus.REJEITADA;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Verifica se a solicitação está pendente de decisão.
     * 
     * @return true se status é PENDENTE
     */
    public boolean isPending() {
        return RequestStatus.PENDENTE.equals(this.status);
    }

    /**
     * Verifica se a solicitação foi aprovada.
     * 
     * @return true se status é APROVADA
     */
    public boolean isApproved() {
        return RequestStatus.APROVADA.equals(this.status);
    }

    /**
     * Verifica se a solicitação foi rejeitada.
     * 
     * @return true se status é REJEITADA
     */
    public boolean isRejected() {
        return RequestStatus.REJEITADA.equals(this.status);
    }
} 