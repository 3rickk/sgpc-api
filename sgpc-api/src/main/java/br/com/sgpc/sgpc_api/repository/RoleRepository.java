package br.com.sgpc.sgpc_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Role;

/**
 * Repository para operações de acesso a dados da entidade Role.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * roles (papéis) no sistema de controle de acesso baseado em roles (RBAC).
 * 
 * Funcionalidades principais:
 * - Busca de roles por nome único
 * - Validação de duplicatas de nomes
 * - Integração com Spring Security
 * - Suporte ao sistema RBAC do SGPC
 * 
 * Roles padrão do sistema:
 * - ADMIN: Acesso total ao sistema
 * - MANAGER: Gerenciamento de projetos e aprovações
 * - USER: Acesso básico às funcionalidades
 * 
 * Características especiais:
 * - Nome do role como identificador único
 * - Busca case-sensitive por questões de segurança
 * - Integração com UserRepository para atribuições
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca um role por nome exato.
     * 
     * Método principal para identificação e recuperação de roles.
     * O nome deve ser único no sistema e é case-sensitive por
     * questões de segurança e consistência.
     * 
     * @param name nome exato do role (ex: "ADMIN", "MANAGER", "USER")
     * @return Optional<Role> role encontrado ou empty se não existir
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica se já existe um role com o nome especificado.
     * 
     * Usado para validação de duplicatas durante criação e edição.
     * Essencial para manter a unicidade dos nomes de roles no sistema.
     * 
     * @param name nome do role a verificar
     * @return boolean true se já existe um role com esse nome
     */
    boolean existsByName(String name);
} 