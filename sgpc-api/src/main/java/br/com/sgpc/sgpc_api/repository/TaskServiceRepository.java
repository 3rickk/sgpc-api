package br.com.sgpc.sgpc_api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.TaskService;

@Repository
public interface TaskServiceRepository extends JpaRepository<TaskService, Long> {

    /**
     * Busca todos os serviços de uma tarefa específica
     */
    List<TaskService> findByTaskId(Long taskId);

    /**
     * Busca todas as tarefas que utilizam um serviço específico
     */
    List<TaskService> findByServiceId(Long serviceId);

    /**
     * Busca serviços de tarefas por projeto
     */
    @Query("SELECT ts FROM TaskService ts WHERE ts.task.project.id = :projectId")
    List<TaskService> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Verifica se uma tarefa já possui um serviço específico
     */
    boolean existsByTaskIdAndServiceId(Long taskId, Long serviceId);

    /**
     * Calcula o custo total de todos os serviços de uma tarefa
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "(COALESCE(ts.unitCostOverride, ts.service.unitLaborCost) + " +
           "ts.service.unitMaterialCost + ts.service.unitEquipmentCost)), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateTotalCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de mão de obra de todos os serviços de uma tarefa
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "COALESCE(ts.unitCostOverride, ts.service.unitLaborCost)), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateLaborCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de materiais de todos os serviços de uma tarefa
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * ts.service.unitMaterialCost), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateMaterialCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Calcula o custo de equipamentos de todos os serviços de uma tarefa
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * ts.service.unitEquipmentCost), 0) " +
           "FROM TaskService ts WHERE ts.task.id = :taskId")
    BigDecimal calculateEquipmentCostByTaskId(@Param("taskId") Long taskId);

    /**
     * Busca serviços de uma tarefa com detalhes completos
     */
    @Query("SELECT ts FROM TaskService ts " +
           "JOIN FETCH ts.service s " +
           "WHERE ts.task.id = :taskId " +
           "ORDER BY s.name")
    List<TaskService> findByTaskIdWithServiceDetails(@Param("taskId") Long taskId);

    /**
     * Remove todos os serviços de uma tarefa
     */
    void deleteByTaskId(Long taskId);

    /**
     * Remove um serviço específico de uma tarefa
     */
    void deleteByTaskIdAndServiceId(Long taskId, Long serviceId);

    /**
     * Busca serviços mais utilizados no projeto
     */
    @Query("SELECT ts.service.id, ts.service.name, COUNT(ts) as usage_count " +
           "FROM TaskService ts " +
           "WHERE ts.task.project.id = :projectId " +
           "GROUP BY ts.service.id, ts.service.name " +
           "ORDER BY usage_count DESC")
    List<Object[]> findMostUsedServicesByProject(@Param("projectId") Long projectId);

    /**
     * Calcula o custo total realizado do projeto (apenas tarefas concluídas)
     */
    @Query("SELECT COALESCE(SUM(ts.quantity * " +
           "(COALESCE(ts.unitCostOverride, ts.service.unitLaborCost) + " +
           "ts.service.unitMaterialCost + ts.service.unitEquipmentCost)), 0) " +
           "FROM TaskService ts " +
           "WHERE ts.task.project.id = :projectId " +
           "AND ts.task.status = 'CONCLUIDA'")
    BigDecimal calculateRealizedCostByProjectId(@Param("projectId") Long projectId);
} 