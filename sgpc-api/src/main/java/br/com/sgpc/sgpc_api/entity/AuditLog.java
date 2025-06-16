package br.com.sgpc.sgpc_api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa logs de auditoria do sistema SGPC.
 * 
 * Esta entidade armazena registros detalhados de todas as operações
 * importantes realizadas no sistema para fins de auditoria, conformidade
 * e rastreabilidade.
 * 
 * Funcionalidades:
 * - Registro de operações CRUD em entidades críticas
 * - Rastreamento de usuário responsável pela operação
 * - Captura de IP para análise de origem
 * - Serialização de valores antes/depois para comparação
 * - Timestamp automático de cada operação
 * - Suporte a auditoria mesmo sem usuário logado
 * 
 * Estrutura de dados:
 * - Identifica qual entidade foi modificada (tipo + ID)
 * - Registra qual operação foi realizada (CREATE, UPDATE, DELETE)
 * - Associa ao usuário responsável quando disponível
 * - Armazena valores em JSON para análise posterior
 * - Inclui metadados de contexto (IP, timestamp)
 * 
 * Integração:
 * - Alimentada automaticamente via AuditAspect (AOP)
 * - Consultada via AuditLogService para relatórios
 * - Indexada por entidade, usuário e timestamp
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * Identificador único do log de auditoria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo da entidade que foi modificada.
     * 
     * Exemplos: "Project", "Task", "User", "MaterialRequest"
     * Usado para filtrar e agrupar logs por tipo de operação.
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * ID da entidade específica que foi modificada.
     * 
     * Combinado com entityType, identifica unicamente
     * o objeto que sofreu a operação.
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Operação realizada na entidade.
     * 
     * Valores padrão: CREATE, UPDATE, DELETE, APPROVE, REJECT
     * Permite identificar o tipo de modificação realizada.
     */
    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    /**
     * Usuário que realizou a operação.
     * 
     * Relacionamento many-to-one opcional, pois algumas
     * operações podem ocorrer sem usuário autenticado
     * (ex: operações de sistema).
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Email do usuário no momento da operação.
     * 
     * Campo desnormalizado para preservar informação mesmo
     * se o usuário for deletado posteriormente. Permite
     * rastreabilidade histórica completa.
     */
    @Column(name = "user_email", length = 255)
    private String userEmail;

    /**
     * Endereço IP de origem da requisição.
     * 
     * Suporta IPv4 e IPv6 (até 45 caracteres).
     * Usado para análise geográfica e detecção de anomalias.
     */
    @Column(name = "user_ip", length = 45)
    private String userIp;

    /**
     * Valores anteriores da entidade em formato JSON.
     * 
     * Serialização do estado da entidade antes da modificação.
     * Permite comparação e reversão de mudanças.
     * Campo opcional (null para operações CREATE).
     */
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    /**
     * Valores posteriores da entidade em formato JSON.
     * 
     * Serialização do estado da entidade após a modificação.
     * Permite comparação e análise de mudanças.
     * Campo opcional (null para operações DELETE).
     */
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    /**
     * Data e hora da operação.
     * 
     * Preenchido automaticamente na criação.
     * Usado para ordenação cronológica e análise temporal.
     */
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    /**
     * Callback executado antes da persistência inicial.
     * 
     * Define automaticamente o timestamp se não foi informado.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Construtor de conveniência para criação de logs de auditoria.
     * 
     * Facilita a criação de logs preenchendo automaticamente
     * campos derivados como userEmail e timestamp.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @param operation operação realizada
     * @param user usuário responsável (pode ser null)
     * @param userIp IP do usuário
     * @param oldValues valores anteriores em JSON
     * @param newValues valores posteriores em JSON
     */
    public AuditLog(String entityType, Long entityId, String operation, User user, String userIp, 
                   String oldValues, String newValues) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.user = user;
        this.userEmail = user != null ? user.getEmail() : null;
        this.userIp = userIp;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Verifica se o log possui valores para comparação.
     * 
     * @return true se possui valores antigos ou novos
     */
    public boolean hasComparisonValues() {
        return oldValues != null || newValues != null;
    }

    /**
     * Obtém descrição legível da operação.
     * 
     * @return String descrição da operação para exibição
     */
    public String getOperationDescription() {
        return switch (operation.toUpperCase()) {
            case "CREATE" -> "Criação";
            case "UPDATE" -> "Atualização";
            case "DELETE" -> "Exclusão";
            case "APPROVE" -> "Aprovação";
            case "REJECT" -> "Rejeição";
            default -> operation;
        };
    }
} 