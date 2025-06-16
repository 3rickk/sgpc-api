package br.com.sgpc.sgpc_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Material;

/**
 * Repository para operações de acesso a dados da entidade Material.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * materiais de construção no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Controle de estoque de materiais
 * - Busca por materiais ativos (soft delete)
 * - Filtros por nome e fornecedor
 * - Alertas de estoque baixo
 * - Validação de duplicatas
 * - Queries otimizadas para performance
 * 
 * Características especiais:
 * - Suporte a soft delete (isActive = false)
 * - Busca case-insensitive para melhor UX
 * - Alertas automáticos de reposição de estoque
 * - Integração com sistema de compras
 * 
 * Todas as queries respeitam o princípio de soft delete,
 * retornando apenas materiais ativos por padrão.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    /**
     * Verifica se já existe um material com o nome especificado.
     * 
     * Usado para validação de duplicatas durante criação e edição.
     * Considera todos os materiais, incluindo inativos.
     * 
     * @param name nome do material a verificar
     * @return boolean true se já existe um material com esse nome
     */
    boolean existsByName(String name);
    
    /**
     * Busca todos os materiais ativos.
     * 
     * Aplica filtro de soft delete, retornando apenas materiais
     * com isActive = true. Usado para listagens gerais.
     * 
     * @return List<Material> lista de materiais ativos
     */
    List<Material> findByIsActiveTrue();
    
    /**
     * Busca materiais ativos por nome (busca parcial, case-insensitive).
     * 
     * Implementa funcionalidade de busca flexível para encontrar
     * materiais quando o nome exato não é conhecido.
     * 
     * @param name parte do nome do material
     * @return List<Material> materiais ativos que contêm o texto no nome
     */
    List<Material> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    /**
     * Busca materiais ativos por fornecedor (busca parcial, case-insensitive).
     * 
     * Permite encontrar todos os materiais de um fornecedor específico
     * para análise de relacionamento comercial e diversificação.
     * 
     * @param supplier parte do nome do fornecedor
     * @return List<Material> materiais ativos do fornecedor
     */
    List<Material> findBySupplierContainingIgnoreCaseAndIsActiveTrue(String supplier);
    
    /**
     * Identifica materiais com estoque abaixo do mínimo.
     * 
     * Query crítica para sistema de alertas de reposição.
     * Compara estoque atual com estoque mínimo definido
     * para cada material ativo.
     * 
     * @return List<Material> materiais que precisam de reposição
     */
    @Query("SELECT m FROM Material m WHERE m.currentStock < m.minimumStock AND m.isActive = true")
    List<Material> findMaterialsBelowMinimumStock();
    
    /**
     * Busca um material ativo por ID.
     * 
     * Versão com soft delete do findById padrão.
     * Garante que apenas materiais ativos sejam retornados.
     * 
     * @param id ID do material
     * @return Optional<Material> material ativo ou empty se não encontrado/inativo
     */
    Optional<Material> findByIdAndIsActiveTrue(Long id);
    
    /**
     * Busca todos os materiais ativos ordenados por nome.
     * 
     * Query otimizada para exibição em dropdowns e listagens
     * onde a ordenação alfabética é importante para UX.
     * 
     * @return List<Material> materiais ativos ordenados alfabeticamente
     */
    @Query("SELECT m FROM Material m WHERE m.isActive = true ORDER BY m.name")
    List<Material> findAllActiveMaterialsOrderedByName();
} 