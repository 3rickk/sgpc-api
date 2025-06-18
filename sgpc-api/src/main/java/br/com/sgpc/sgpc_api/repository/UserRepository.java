package br.com.sgpc.sgpc_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.User;

/**
 * Repository para operações de acesso a dados da entidade User.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * usuários e autenticação no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Autenticação por email
 * - Busca de usuários com roles carregados
 * - Validação de duplicatas de email
 * - Filtro por usuários ativos (soft delete)
 * - Queries otimizadas para performance
 * 
 * Características especiais:
 * - Integração com Spring Security
 * - Suporte a soft delete (isActive = false)
 * - Fetch join com roles para evitar N+1
 * - Email como identificador único
 * 
 * Segurança:
 * - Email sempre em lowercase para consistência
 * - Validação de unicidade rigorosa
 * - Controle de acesso baseado em roles
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca um usuário por email.
     * 
     * Método principal para autenticação e identificação de usuários.
     * O email deve ser único no sistema e serve como login.
     * 
     * @param email endereço de email do usuário
     * @return Optional<User> usuário encontrado ou empty se não existir
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se já existe um usuário com o email especificado.
     * 
     * Usado para validação de duplicatas durante criação e edição.
     * Essencial para manter a unicidade do email no sistema.
     * 
     * @param email endereço de email a verificar
     * @return boolean true se já existe um usuário com esse email
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca um usuário por email com roles carregados.
     * 
     * Versão otimizada que utiliza JOIN FETCH para carregar
     * os roles do usuário em uma única query, evitando o
     * problema N+1. Usado para autenticação e autorização.
     * 
     * @param email endereço de email do usuário
     * @return Optional<User> usuário com roles carregados
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);
    
    /**
     * Busca um usuário por ID com roles carregados.
     * 
     * Versão otimizada que utiliza JOIN FETCH para carregar
     * os roles do usuário em uma única query, evitando o
     * problema N+1. Usado para operações que precisam dos roles.
     * 
     * @param id ID do usuário
     * @return Optional<User> usuário com roles carregados
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(Long id);
    
    /**
     * Remove todos os roles de um usuário.
     * 
     * Usado para atualização de roles sem conflitos de chave primária.
     * 
     * @param userId ID do usuário
     */
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId", nativeQuery = true)
    void removeAllUserRoles(Long userId);
    
    /**
     * Busca todos os usuários ativos.
     * 
     * Aplica filtro de soft delete, retornando apenas usuários
     * com isActive = true. Usado para listagens e seleções.
     * 
     * @return Iterable<User> usuários ativos do sistema
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Iterable<User> findAllActiveUsers();
} 