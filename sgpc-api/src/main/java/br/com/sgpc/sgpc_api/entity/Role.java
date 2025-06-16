package br.com.sgpc.sgpc_api.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa roles (papéis) de usuários no sistema.
 * 
 * Esta entidade define os diferentes níveis de acesso e permissões
 * que os usuários podem ter no sistema SGPC. Utiliza um modelo
 * de many-to-many com usuários, permitindo que um usuário tenha
 * múltiplos roles e um role seja atribuído a múltiplos usuários.
 * 
 * Roles padrão do sistema:
 * - ADMIN: Acesso total ao sistema, gerenciamento de usuários
 * - MANAGER: Gerenciamento de projetos, aprovação de solicitações
 * - USER: Acesso básico, criação de tarefas e solicitações
 * 
 * Funcionalidades:
 * - Controle de acesso baseado em roles (RBAC)
 * - Flexibilidade para novos roles no futuro
 * - Auditoria com timestamps automáticos
 * - Relacionamento bidirecional com usuários
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * Identificador único do role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome único do role.
     * 
     * Usado para identificação e verificação de permissões.
     * Deve ser único no sistema e preferencialmente em maiúsculas.
     * Exemplos: "ADMIN", "MANAGER", "USER"
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Descrição detalhada do role.
     * 
     * Explica as responsabilidades e permissões do role
     * para facilitar o entendimento administrativo.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Data e hora de criação do role.
     * 
     * Preenchida automaticamente na criação.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização.
     * 
     * Atualizada automaticamente em modificações.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Usuários que possuem este role.
     * 
     * Relacionamento many-to-many bidirecional.
     * Um role pode ser atribuído a múltiplos usuários.
     * Lazy loading para otimização de performance.
     */
    @ManyToMany(mappedBy = "roles", fetch = jakarta.persistence.FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /**
     * Callback executado antes da persistência inicial.
     * 
     * Define automaticamente as timestamps de criação e atualização.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Callback executado antes de cada atualização.
     * 
     * Atualiza automaticamente o timestamp de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se este é o role de administrador.
     * 
     * @return true se for role ADMIN
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.name);
    }

    /**
     * Verifica se este é o role de gerente.
     * 
     * @return true se for role MANAGER
     */
    public boolean isManager() {
        return "MANAGER".equals(this.name);
    }

    /**
     * Verifica se este é o role de usuário padrão.
     * 
     * @return true se for role USER
     */
    public boolean isUser() {
        return "USER".equals(this.name);
    }

    /**
     * Construtor de conveniência para criação de roles.
     * 
     * @param name nome do role
     * @param description descrição do role
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
} 