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
 * Entidade que representa tokens de redefinição de senha.
 * 
 * Esta entidade armazena tokens temporários gerados para permitir
 * que usuários redefinam suas senhas de forma segura através de
 * um link enviado por email.
 * 
 * Funcionalidades principais:
 * - Tokens únicos por usuário
 * - Controle de expiração (24 horas)
 * - Verificação automática de validade
 * - Timestamps de criação automáticos
 * - Relacionamento com usuário
 * 
 * Características de segurança:
 * - Token único no sistema (constraint unique)
 * - Expiração automática para limitar janela de ataque
 * - Método isExpired() para validação
 * - Lazy loading do usuário para performance
 * 
 * Ciclo de vida:
 * 1. Criado quando usuário solicita reset
 * 2. Enviado por email (implementação futura)
 * 3. Validado quando usuário acessa link
 * 4. Removido após uso ou expiração
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    /**
     * Identificador único do token no banco de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Usuário proprietário do token de reset.
     * 
     * Relacionamento many-to-one com lazy loading para performance.
     * Um usuário pode ter apenas um token ativo por vez.
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Token único para redefinição de senha.
     * 
     * Gerado com UUID para garantir unicidade e segurança.
     * Constraint unique no banco impede duplicatas.
     */
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    
    /**
     * Data e hora de expiração do token.
     * 
     * Tokens são válidos por 24 horas após criação.
     * Após expiração, não podem mais ser utilizados.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Data e hora de criação do token.
     * 
     * Preenchida automaticamente no método onCreate().
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Callback executado antes da persistência inicial.
     * 
     * Define automaticamente a data de criação.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o token está expirado.
     * 
     * Compara a data de expiração com o momento atual
     * para determinar se o token ainda é válido.
     * 
     * @return boolean true se o token está expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
} 