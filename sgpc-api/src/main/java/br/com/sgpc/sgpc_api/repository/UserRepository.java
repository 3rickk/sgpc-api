package br.com.sgpc.sgpc_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca um usuário por email.
     * 
     * @param email endereço de email do usuário
     * @return Optional<User> usuário encontrado ou empty se não existir
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se já existe um usuário com o email especificado.
     * 
     * @param email endereço de email a verificar
     * @return boolean true se já existe um usuário com esse email
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca os nomes das roles de um usuário por email usando JPQL.
     * 
     * @param email endereço de email do usuário
     * @return List<String> nomes das roles do usuário
     */
    @Query("SELECT r.name FROM User u JOIN u.roles r WHERE u.email = :email")
    List<String> findRoleNamesByEmail(@Param("email") String email);
    
    /**
     * Remove todos os roles de um usuário.
     * 
     * @param userId ID do usuário
     */
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId", nativeQuery = true)
    void removeAllUserRoles(@Param("userId") Long userId);
    
    /**
     * Busca todos os usuários ativos.
     * 
     * @return Iterable<User> usuários ativos do sistema
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Iterable<User> findAllActiveUsers();
} 