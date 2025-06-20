package br.com.sgpc.sgpc_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.enums.ProjectStatus;

/**
 * Repository para operações de acesso a dados da entidade Project.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de projetos
 * de construção civil no sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Busca de projetos com equipe carregada (eager loading)
 * - Filtragem por status (EM_ANDAMENTO, PAUSADO, CONCLUIDO, etc.)
 * - Busca por cliente para análise de portfolio
 * - Identificação de projetos em atraso
 * - Queries para membros da equipe
 * - Contadores para dashboards executivos
 * - Busca por faixas de datas para planejamento
 * - Validação de duplicatas por nome
 * 
 * Todas as queries utilizam JPQL otimizadas com fetch joins
 * para evitar o problema N+1 e melhorar performance.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Busca um projeto por ID com membros da equipe carregados.
     * 
     * Utiliza LEFT JOIN FETCH para carregar todos os membros da equipe
     * em uma única query, evitando lazy loading e problema N+1.
     * 
     * @param id ID do projeto
     * @return Optional<Project> projeto com equipe carregada
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.teamMembers WHERE p.id = :id")
    Optional<Project> findByIdWithTeamMembers(@Param("id") Long id);

    /**
     * Busca projetos por status específico.
     * 
     * Útil para dashboards executivos e relatórios gerenciais.
     * Permite análise de projetos ativos, pausados, concluídos, etc.
     * 
     * @param status status do projeto
     * @return List<Project> projetos no status especificado
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    List<Project> findByStatus(@Param("status") ProjectStatus status);

    /**
     * Busca projetos por cliente.
     * 
     * Permite análise do portfolio de projetos por cliente,
     * histórico de trabalhos e relacionamento comercial.
     * 
     * @param client nome do cliente
     * @return List<Project> projetos do cliente
     */
    @Query("SELECT p FROM Project p WHERE p.client = :client")
    List<Project> findByClient(@Param("client") String client);

    /**
     * Busca projetos por nome (busca parcial, case-insensitive).
     * 
     * Implementa funcionalidade de busca flexível para encontrar
     * projetos quando o nome exato não é conhecido.
     * 
     * @param name parte do nome do projeto
     * @return List<Project> projetos que contêm o texto no nome
     */
    @Query("SELECT p FROM Project p WHERE p.name LIKE %:name%")
    List<Project> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Identifica projetos em atraso.
     * 
     * Busca projetos onde a data de entrega planejada já passou
     * e o status não é CONCLUIDO nem CANCELADO.
     * 
     * @param date data de referência (normalmente LocalDate.now())
     * @return List<Project> projetos em atraso
     */
    @Query("SELECT p FROM Project p WHERE p.endDatePlanned < :date AND p.status NOT IN ('CONCLUIDO', 'CANCELADO')")
    List<Project> findDelayedProjects(@Param("date") LocalDate date);

    /**
     * Busca projetos onde um usuário é membro da equipe.
     * 
     * Utiliza JOIN na tabela de relacionamento many-to-many
     * para encontrar todos os projetos de um membro específico.
     * 
     * @param userId ID do usuário/membro da equipe
     * @return List<Project> projetos onde o usuário participa
     */
    @Query("SELECT p FROM Project p JOIN p.teamMembers tm WHERE tm.id = :userId")
    List<Project> findProjectsByTeamMemberId(@Param("userId") Long userId);

    /**
     * Conta projetos por status.
     * 
     * Usado para métricas executivas e dashboards mostrando
     * distribuição de projetos por status.
     * 
     * @param status status do projeto
     * @return Long número de projetos no status
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") ProjectStatus status);

    /**
     * Busca projetos programados para iniciar em um período.
     * 
     * Usado para planejamento de recursos e cronograma de novos projetos.
     * Considera apenas a data de início planejada.
     * 
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return List<Project> projetos que iniciam no período
     */
    @Query("SELECT p FROM Project p WHERE p.startDatePlanned BETWEEN :startDate AND :endDate")
    List<Project> findProjectsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Verifica se já existe um projeto com o mesmo nome.
     * 
     * Usado para validação de duplicatas e garantir nomes únicos
     * de projetos no sistema.
     * 
     * @param name nome do projeto
     * @return boolean true se já existe um projeto com esse nome
     */
    boolean existsByName(String name);

    /**
     * Conta o número de membros na equipe de um projeto específico.
     * 
     * @param projectId ID do projeto
     * @return número de membros na equipe
     */
    @Query(value = "SELECT COUNT(*) FROM project_members WHERE project_id = :projectId", nativeQuery = true)
    Long countTeamMembersByProjectId(@Param("projectId") Long projectId);

    /**
     * Busca projetos acessíveis para um usuário específico baseado em sua role.
     * 
     * Para ADMIN: projetos que criou ou está na equipe
     * Para MANAGER: projetos onde está na equipe
     * Para USER: projetos onde está na equipe
     * 
     * @param userId ID do usuário
     * @param userRole role do usuário (ADMIN, MANAGER, USER)
     * @return List<Project> projetos acessíveis pelo usuário
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE (:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId)")
    List<Project> findProjectsAccessibleByUser(@Param("userId") Long userId, @Param("userRole") String userRole);

    /**
     * Busca projetos por status acessíveis para um usuário específico.
     * 
     * @param status status do projeto
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return List<Project> projetos no status especificado e acessíveis pelo usuário
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE p.status = :status AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    List<Project> findByStatusAccessibleByUser(@Param("status") ProjectStatus status, 
                                               @Param("userId") Long userId, 
                                               @Param("userRole") String userRole);

    /**
     * Busca projetos por cliente acessíveis para um usuário específico.
     * 
     * @param client nome do cliente
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return List<Project> projetos do cliente acessíveis pelo usuário
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE p.client = :client AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    List<Project> findByClientAccessibleByUser(@Param("client") String client, 
                                               @Param("userId") Long userId, 
                                               @Param("userRole") String userRole);

    /**
     * Busca projetos por nome acessíveis para um usuário específico.
     * 
     * @param name parte do nome do projeto
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return List<Project> projetos que contêm o texto no nome e são acessíveis pelo usuário
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE p.name LIKE %:name% AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    List<Project> findByNameContainingIgnoreCaseAccessibleByUser(@Param("name") String name, 
                                                                 @Param("userId") Long userId, 
                                                                 @Param("userRole") String userRole);

    /**
     * Busca projetos atrasados acessíveis para um usuário específico.
     * 
     * @param date data de referência
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return List<Project> projetos em atraso acessíveis pelo usuário
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE p.endDatePlanned < :date AND p.status NOT IN ('CONCLUIDO', 'CANCELADO') AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    List<Project> findDelayedProjectsAccessibleByUser(@Param("date") LocalDate date, 
                                                      @Param("userId") Long userId, 
                                                      @Param("userRole") String userRole);

    /**
     * Verifica se um projeto é acessível para um usuário específico.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return boolean true se o usuário pode acessar o projeto
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p LEFT JOIN p.teamMembers tm " +
           "WHERE p.id = :projectId AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    boolean isProjectAccessibleByUser(@Param("projectId") Long projectId, 
                                      @Param("userId") Long userId, 
                                      @Param("userRole") String userRole);

    /**
     * Busca um projeto por ID com verificação de acesso do usuário.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @param userRole role do usuário
     * @return Optional<Project> projeto se acessível pelo usuário
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.teamMembers tm " +
           "WHERE p.id = :projectId AND " +
           "((:userRole = 'ADMIN' AND (p.createdBy.id = :userId OR tm.id = :userId)) " +
           "OR (:userRole IN ('MANAGER', 'USER') AND tm.id = :userId))")
    Optional<Project> findByIdAccessibleByUser(@Param("projectId") Long projectId, 
                                               @Param("userId") Long userId, 
                                               @Param("userRole") String userRole);
} 