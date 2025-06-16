package br.com.sgpc.sgpc_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.TaskService;

/**
 * Repository para operações de acesso a dados da entidade TaskService.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento da
 * relação entre tarefas e serviços no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Relacionamento tarefa-serviço
 * - Cálculos de custos por categoria
 * - Análise de uso de serviços
 * - Controle de duplicatas
 * - Custos realizados vs planejados
 * - Relatórios de execução
 * 
 * Características especiais:
 * - Suporte a override de custos unitários
 * - Cálculos automáticos por categoria
 * - Queries otimizadas com JOIN FETCH
 * - Análise estatística de uso
 * - Integração com sistema de custos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface TaskServiceRepository extends JpaRepository<TaskService, Long> {

    /**
     * Busca todos os serviços de uma tarefa específica.
     * 
     * Retorna lista completa de serviços executados em uma tarefa.
     * Usado para visualização detalhada de execução e custos.
     * 
     * @param taskId ID da tarefa
     * @return List<TaskService> lista de serviços da tarefa
     */
    List<TaskService> findByTaskId(Long taskId);

    /**
     * Busca todas as tarefas que utilizam um serviço específico.
     * 
     * Permite rastrear onde um serviço é utilizado no projeto.
     * Útil para análise de impacto e alterações de preços.
     * 
     * @param serviceId ID do serviço
     * @return List<TaskService> lista de utilizações do serviço
     */
    List<TaskService> findByServiceId(Long serviceId);

    /**
     * Busca serviços de tarefas por projeto específico.
     * 
     * Retorna todos os serviços executados em todas as tarefas
     * de um projeto. Usado para análise geral do projeto.
     * 
     * @param projectId ID do projeto
     * @return List<TaskService> todos os serviços do projeto
     */
    @Query("SELECT ts FROM TaskService ts WHERE ts.task.project.id = :projectId")
    List<TaskService> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Verifica se uma tarefa já possui um serviço específico.
     * 
     * Usado para validação de duplicatas ao adicionar serviços
     * a uma tarefa, evitando execuções repetidas.
     * 
     * @param taskId ID da tarefa
     * @param serviceId ID do serviço
     * @return boolean true se a tarefa já possui o serviço
     */
    boolean existsByTaskIdAndServiceId(Long taskId, Long serviceId);

    /**
     * Calcula o custo total de todos os serviços de uma tarefa.
     * 
     * Soma todos os custos (mão de obra + material + equipamento)
     * considerando quantidade e override de custos quando aplicável.
     * 
     * @param taskId ID da tarefa
     * @return BigDecimal custo total da tarefa
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "(COALESCE(ts.unitCostOverride, ts.service.unitLaborCost) + " +
           "ts.service.unitMaterialCost + ts.service.unitEquipmentCost)), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateTotalCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de mão de obra de todos os serviços de uma tarefa.
     * 
     * Considera override de custo unitário quando disponível,
     * senão usa o custo padrão de mão de obra do serviço.
     * 
     * @param taskId ID da tarefa
     * @return BigDecimal custo total de mão de obra
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "COALESCE(ts.unitCostOverride, ts.service.unitLaborCost)), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateLaborCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de materiais de todos os serviços de uma tarefa.
     * 
     * Soma custos de materiais sem considerar override, pois
     * o override se aplica apenas à mão de obra.
     * 
     * @param taskId ID da tarefa
     * @return BigDecimal custo total de materiais
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * ts.service.unitMaterialCost), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateMaterialCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de equipamentos de todos os serviços de uma tarefa.
     * 
     * Soma custos de equipamentos sem considerar override.
     * 
     * @param taskId ID da tarefa
     * @return BigDecimal custo total de equipamentos
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * ts.service.unitEquipmentCost), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateEquipmentCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Busca serviços de uma tarefa com detalhes completos do serviço.
     * 
     * Query otimizada com JOIN FETCH para evitar problema N+1.
     * Retorna dados completos para exibição detalhada.
     * 
     * @param taskId ID da tarefa
     * @return List<TaskService> serviços com detalhes carregados
     */
    @Query("SELECT ts FROM TaskService ts " +
           "JOIN FETCH ts.service s " +
           "WHERE ts.task.id = :taskId " +
           "ORDER BY s.name")
    List<TaskService> findByTaskIdWithServiceDetails(@Param("taskId") Long taskId);

    /**
     * Remove todos os serviços de uma tarefa.
     * 
     * Usado quando uma tarefa é excluída ou quando
     * todos os serviços precisam ser redefinidos.
     * 
     * @param taskId ID da tarefa
     */
    void deleteByTaskId(Long taskId);

    /**
     * Remove um serviço específico de uma tarefa.
     * 
     * Usado para remover execuções específicas sem
     * afetar outros serviços da mesma tarefa.
     * 
     * @param taskId ID da tarefa
     * @param serviceId ID do serviço
     */
    void deleteByTaskIdAndServiceId(Long taskId, Long serviceId);

    /**
     * Busca serviços mais utilizados no projeto.
     * 
     * Retorna estatística de uso de serviços ordenados por
     * frequência de uso. Útil para análise de padrões.
     * 
     * @param projectId ID do projeto
     * @return List<Object[]> array com [serviceId, serviceName, usageCount]
     */
    @Query("SELECT ts.service.id, ts.service.name, COUNT(ts) as usage_count " +
           "FROM TaskService ts " +
           "WHERE ts.task.project.id = :projectId " +
           "GROUP BY ts.service.id, ts.service.name " +
           "ORDER BY usage_count DESC")
    List<Object[]> findMostUsedServicesByProject(@Param("projectId") Long projectId);

    /**
     * Calcula o custo total realizado do projeto (apenas tarefas concluídas).
     * 
     * Considera apenas tarefas com status CONCLUIDA para calcular
     * custo realmente executado vs planejado do projeto.
     * 
     * @param projectId ID do projeto
     * @return BigDecimal custo realizado até o momento
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "(COALESCE(ts.unitCostOverride, ts.service.unitLaborCost) + " +
           "ts.service.unitMaterialCost + ts.service.unitEquipmentCost)), 0) " +
           "FROM TaskService ts " +
           "WHERE ts.task.project.id = :projectId " +
           "AND ts.task.status = 'CONCLUIDA'")
    BigDecimal calculateRealizedCostByProjectId(@Param("projectId") Long projectId);
} 