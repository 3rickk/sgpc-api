package br.com.sgpc.sgpc_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.enums.TaskStatus;

/**
 * Repository para operações de acesso a dados da entidade Task.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de tarefas
 * no sistema Kanban do SGPC.
 * 
 * Funcionalidades principais:
 * - Busca de tarefas por projeto com ordenação por prioridade
 * - Filtragem por status (A_FAZER, EM_ANDAMENTO, CONCLUIDA)
 * - Queries para usuários atribuídos
 * - Identificação de tarefas em atraso
 * - Contadores para dashboards e métricas
 * - Busca por faixas de datas para cronogramas
 * - Validação de duplicatas
 * 
 * Todas as queries utilizam JPQL para compatibilidade com diferentes
 * bancos de dados e são otimizadas com fetch joins para performance.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Busca uma tarefa por ID com todos os relacionamentos carregados.
     * 
     * Utiliza LEFT JOIN FETCH para carregar projeto, usuário atribuído e
     * usuário criador em uma única query, evitando o problema N+1.
     * 
     * @param id ID da tarefa
     * @return Optional<Task> tarefa com relacionamentos carregados
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedUser LEFT JOIN FETCH t.createdByUser WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca todas as tarefas de um projeto ordenadas por prioridade.
     * 
     * Ordenação: prioridade descrescente (mais alta primeiro),
     * depois por data de criação ascendente (mais antigas primeiro).
     * 
     * @param projectId ID do projeto
     * @return List<Task> tarefas do projeto ordenadas
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Busca tarefas de um projeto por status específico.
     * 
     * Útil para implementar o board Kanban separando tarefas por colunas:
     * A_FAZER, EM_ANDAMENTO, CONCLUIDA.
     * 
     * @param projectId ID do projeto
     * @param status status da tarefa
     * @return List<Task> tarefas filtradas por status
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    /**
     * Busca todas as tarefas atribuídas a um usuário.
     * 
     * Ordenação: prioridade descrescente, depois por data de entrega planejada
     * ascendente (mais urgentes primeiro).
     * 
     * @param userId ID do usuário
     * @return List<Task> tarefas atribuídas ao usuário
     */
    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId ORDER BY t.priority DESC, t.endDatePlanned ASC")
    List<Task> findByAssignedUserId(@Param("userId") Long userId);

    /**
     * Busca tarefas atribuídas a um usuário por status.
     * 
     * Útil para dashboards pessoais e views específicas do usuário.
     * 
     * @param userId ID do usuário
     * @param status status da tarefa
     * @return List<Task> tarefas filtradas por usuário e status
     */
    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status ORDER BY t.priority DESC, t.endDatePlanned ASC")
    List<Task> findByAssignedUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    /**
     * Identifica tarefas em atraso.
     * 
     * Busca tarefas onde a data de entrega planejada já passou e o status
     * não é CONCLUIDA nem CANCELADA.
     * 
     * @param date data de referência (normalmente LocalDate.now())
     * @return List<Task> tarefas em atraso
     */
    @Query("SELECT t FROM Task t WHERE t.endDatePlanned < :date AND t.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);

    /**
     * Identifica tarefas em atraso de um projeto específico.
     * 
     * Versão filtrada da query de atraso para análise por projeto.
     * 
     * @param projectId ID do projeto
     * @param date data de referência
     * @return List<Task> tarefas em atraso do projeto
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.endDatePlanned < :date AND t.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    List<Task> findOverdueTasksByProject(@Param("projectId") Long projectId, @Param("date") LocalDate date);

    /**
     * Conta o total de tarefas de um projeto.
     * 
     * Usado para métricas e dashboards mostrando o progresso do projeto.
     * 
     * @param projectId ID do projeto
     * @return Long número total de tarefas
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * Conta tarefas de um projeto por status.
     * 
     * Usado para calcular percentual de conclusão e métricas do Kanban.
     * 
     * @param projectId ID do projeto
     * @param status status da tarefa
     * @return Long número de tarefas no status especificado
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    /**
     * Conta tarefas atribuídas a um usuário por status.
     * 
     * Usado para dashboards pessoais e métricas de produtividade.
     * 
     * @param userId ID do usuário
     * @param status status da tarefa
     * @return Long número de tarefas do usuário no status
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    Long countByAssignedUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    /**
     * Busca tarefas de um projeto por nível de prioridade.
     * 
     * Útil para análise de carga de trabalho e identificação de
     * tarefas críticas.
     * 
     * @param projectId ID do projeto
     * @param priority nível de prioridade (1-5)
     * @return List<Task> tarefas com a prioridade especificada
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority = :priority ORDER BY t.createdAt ASC")
    List<Task> findByProjectIdAndPriority(@Param("projectId") Long projectId, @Param("priority") Integer priority);

    /**
     * Busca tarefas programadas para um período específico.
     * 
     * Inclui tarefas que começam OU terminam no período especificado.
     * Usado para cronogramas e planejamento de recursos.
     * 
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return List<Task> tarefas no período
     */
    @Query("SELECT t FROM Task t WHERE t.startDatePlanned BETWEEN :startDate AND :endDate OR t.endDatePlanned BETWEEN :startDate AND :endDate")
    List<Task> findTasksInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Busca tarefas de um projeto específico em um período.
     * 
     * Versão filtrada da query de período para análise por projeto.
     * 
     * @param projectId ID do projeto
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return List<Task> tarefas do projeto no período
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND (t.startDatePlanned BETWEEN :startDate AND :endDate OR t.endDatePlanned BETWEEN :startDate AND :endDate)")
    List<Task> findTasksByProjectInDateRange(@Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Verifica se já existe uma tarefa com o mesmo título no projeto.
     * 
     * Usado para validação de duplicatas e garantir títulos únicos
     * dentro do contexto do projeto.
     * 
     * @param projectId ID do projeto
     * @param title título da tarefa
     * @return boolean true se já existe uma tarefa com esse título
     */
    boolean existsByProjectIdAndTitle(@Param("projectId") Long projectId, @Param("title") String title);
} 