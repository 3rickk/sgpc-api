package br.com.sgpc.sgpc_api.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.PasswordResetToken;

/**
 * Repository para operações de acesso a dados da entidade PasswordResetToken.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * tokens de redefinição de senha no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Busca de tokens por string única
 * - Remoção de tokens por usuário
 * - Limpeza automática de tokens expirados
 * - Prevenção de acúmulo de tokens antigos
 * 
 * Características especiais:
 * - Queries otimizadas para operações de segurança
 * - Suporte a limpeza automática por expiração
 * - Métodos para manutenção preventiva
 * - Integração com sistema de reset de senha
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Busca um token de reset por sua string única.
     * 
     * Método principal para validação de tokens durante
     * o processo de redefinição de senha.
     * 
     * @param token string do token a ser buscado
     * @return Optional<PasswordResetToken> token encontrado ou empty
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Remove todos os tokens de um usuário específico.
     * 
     * Usado para limpar tokens antigos antes de gerar
     * um novo, evitando acúmulo de tokens não utilizados.
     * 
     * @param userId ID do usuário cujos tokens serão removidos
     */
    void deleteByUser_Id(Long userId);
    
    /**
     * Remove todos os tokens expirados do sistema.
     * 
     * Método de limpeza automática que deve ser executado
     * periodicamente para manter a base de dados limpa.
     * 
     * @param now data/hora atual para comparação de expiração
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
} 