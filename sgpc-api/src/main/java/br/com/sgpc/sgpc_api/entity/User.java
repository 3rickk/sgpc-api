package br.com.sgpc.sgpc_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidade que representa um usuário do sistema.
 * 
 * Esta entidade modela os usuários que podem acessar o sistema SGPC,
 * incluindo informações pessoais, credenciais de acesso, configurações
 * profissionais e relacionamentos com roles de segurança.
 * 
 * Principais características:
 * - Autenticação baseada em email e senha hash
 * - Sistema de roles para controle de acesso
 * - Taxa horária para cálculos de custo de mão de obra
 * - Status ativo/inativo para controle de acesso
 * - Relacionamento many-to-many com roles
 * - Auditoria automática de criação e atualização
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Identificador único do usuário.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do usuário.
     * 
     * Nome que será exibido na interface e relatórios.
     */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Email do usuário para login e comunicação.
     * 
     * Deve ser único no sistema e serve como identificador
     * principal para autenticação.
     */
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * Telefone do usuário para contato.
     * 
     * Campo opcional usado para comunicação e contato.
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Hash da senha do usuário.
     * 
     * Senha criptografada para autenticação segura.
     * Nunca deve ser exposta em APIs ou logs.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Taxa horária do usuário para cálculos de custo.
     * 
     * Valor por hora utilizado para calcular custos de
     * mão de obra em projetos e tarefas.
     */
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * Indica se o usuário está ativo no sistema.
     * 
     * Usuários inativos não podem fazer login, mas seus
     * dados são preservados para histórico.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Data e hora de criação da conta.
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
     * Roles/funções do usuário no sistema.
     * 
     * Define as permissões e níveis de acesso que o usuário
     * possui. Um usuário pode ter múltiplas roles.
     */
    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Método executado antes da persistência.
     * 
     * Inicializa timestamps de criação e atualização.
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
     * Adiciona uma role ao usuário.
     * 
     * Estabelece relacionamento bidirecional entre usuário e role,
     * garantindo consistência dos dados.
     * 
     * @param role role a ser adicionada ao usuário
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * Remove uma role do usuário.
     * 
     * Remove relacionamento bidirecional entre usuário e role,
     * mantendo integridade dos dados.
     * 
     * @param role role a ser removida do usuário
     */
    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * Verifica se o usuário possui uma role específica.
     * 
     * @param roleName nome da role a verificar
     * @return true se o usuário possui a role, false caso contrário
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Verifica se o usuário está ativo e pode fazer login.
     * 
     * @return true se o usuário está ativo
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Calcula o custo por hora do usuário.
     * 
     * @return BigDecimal taxa horária ou ZERO se não configurada
     */
    public BigDecimal getHourlyCost() {
        return hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
    }
} 