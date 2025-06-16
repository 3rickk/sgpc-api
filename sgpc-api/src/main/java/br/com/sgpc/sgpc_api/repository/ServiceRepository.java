package br.com.sgpc.sgpc_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Service;

/**
 * Repository para operações de acesso a dados da entidade Service.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * serviços de construção no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Busca por serviços ativos (soft delete)
 * - Filtros por nome e unidade de medida
 * - Queries por faixas de custo
 * - Validação de duplicatas
 * - Ordenação otimizada
 * - Análise de custos unitários
 * 
 * Características especiais:
 * - Suporte a soft delete (isActive = false)
 * - Busca case-insensitive para melhor UX
 * - Cálculos automáticos de custo total
 * - Queries otimizadas para performance
 * - Integração com sistema de custos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Busca serviços por nome (busca parcial, case-insensitive).
     * 
     * Implementa funcionalidade de busca flexível para encontrar
     * serviços quando o nome exato não é conhecido.
     * 
     * @param name parte do nome do serviço
     * @return List<Service> serviços que contêm o texto no nome
     */
    List<Service> findByNameContainingIgnoreCase(String name);

    /**
     * Busca todos os serviços ativos.
     * 
     * Aplica filtro de soft delete, retornando apenas serviços
     * com isActive = true. Usado para listagens gerais.
     * 
     * @return List<Service> lista de serviços ativos
     */
    List<Service> findByIsActiveTrue();

    /**
     * Busca todos os serviços inativos.
     * 
     * Usado para análise de serviços descontinuados ou
     * recuperação de dados históricos.
     * 
     * @return List<Service> lista de serviços inativos
     */
    List<Service> findByIsActiveFalse();

    /**
     * Busca serviços por unidade de medida específica.
     * 
     * Permite agrupar serviços por tipo de medição
     * (m², m³, horas, etc.) para análise e relatórios.
     * 
     * @param unitOfMeasurement unidade de medida a buscar
     * @return List<Service> serviços com a unidade especificada
     */
    List<Service> findByUnitOfMeasurement(String unitOfMeasurement);

    /**
     * Busca serviços ativos por unidade de medida.
     * 
     * Versão com filtro de soft delete da busca por unidade.
     * 
     * @param unitOfMeasurement unidade de medida a buscar
     * @return List<Service> serviços ativos com a unidade especificada
     */
    List<Service> findByUnitOfMeasurementAndIsActiveTrue(String unitOfMeasurement);

    /**
     * Verifica se já existe um serviço com o nome especificado.
     * 
     * Usado para validação de duplicatas durante criação.
     * Considera todos os serviços, incluindo inativos.
     * 
     * @param name nome do serviço a verificar
     * @return boolean true se já existe um serviço com esse nome
     */
    boolean existsByName(String name);

    /**
     * Verifica se existe serviço com nome (excluindo ID específico).
     * 
     * Usado para validação de duplicatas durante edição,
     * permitindo que o próprio serviço mantenha seu nome.
     * 
     * @param name nome do serviço a verificar
     * @param id ID do serviço a excluir da verificação
     * @return boolean true se existe outro serviço com esse nome
     */
    @Query("SELECT COUNT(s) > 0 FROM Service s WHERE s.name = :name AND s.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Busca serviço por nome exato.
     * 
     * Método para identificação precisa de serviços
     * quando o nome completo é conhecido.
     * 
     * @param name nome exato do serviço
     * @return Optional<Service> serviço encontrado ou empty
     */
    Optional<Service> findByName(String name);

    /**
     * Busca todos os serviços ordenados por nome.
     * 
     * Query otimizada para exibição em listagens onde
     * a ordenação alfabética é importante.
     * 
     * @return List<Service> todos os serviços ordenados alfabeticamente
     */
    @Query("SELECT s FROM Service s ORDER BY s.name ASC")
    List<Service> findAllOrderByName();

    /**
     * Busca serviços ativos ordenados por nome.
     * 
     * Versão com soft delete da busca ordenada.
     * Ideal para dropdowns e seleções de usuário.
     * 
     * @return List<Service> serviços ativos ordenados alfabeticamente
     */
    @Query("SELECT s FROM Service s WHERE s.isActive = true ORDER BY s.name ASC")
    List<Service> findActiveServicesOrderByName();

    /**
     * Busca serviços por faixa de custo unitário total.
     * 
     * Calcula o custo total (mão de obra + material + equipamento)
     * e filtra serviços dentro da faixa especificada.
     * Usado para análise de custos e orçamentação.
     * 
     * @param minCost custo mínimo (inclusive)
     * @param maxCost custo máximo (inclusive)
     * @return List<Service> serviços na faixa de custo
     */
    @Query("SELECT s FROM Service s WHERE (s.unitLaborCost + s.unitMaterialCost + s.unitEquipmentCost) BETWEEN :minCost AND :maxCost")
    List<Service> findByTotalUnitCostBetween(@Param("minCost") Double minCost, @Param("maxCost") Double maxCost);

    /**
     * Busca unidades de medida distintas dos serviços ativos.
     * 
     * Query para popular dropdowns e filtros com as unidades
     * de medida disponíveis no sistema. Ordenado alfabeticamente.
     * 
     * @return List<String> lista de unidades de medida únicas
     */
    @Query("SELECT DISTINCT s.unitOfMeasurement FROM Service s WHERE s.isActive = true ORDER BY s.unitOfMeasurement")
    List<String> findDistinctUnitOfMeasurement();
} 