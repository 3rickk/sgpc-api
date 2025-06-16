package br.com.sgpc.sgpc_api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.AuditLog;

/**
 * Repository para operações de acesso a dados da entidade AuditLog.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o sistema de auditoria
 * do SGPC. Permite rastreamento completo de operações no sistema.
 * 
 * Funcionalidades principais:
 * - Busca por entidade e ID específicos
 * - Filtros por usuário e operação
 * - Queries por período de tempo
 * - Paginação para grandes volumes
 * - Relatórios de auditoria com filtros
 * - Ordenação cronológica automática
 * 
 * Características especiais:
 * - Todas as queries ordenadas por timestamp DESC
 * - Suporte a filtros combinados para relatórios
 * - Integração com sistema AOP de auditoria
 * - Performance otimizada para grandes volumes
 * - Queries flexíveis com parâmetros opcionais
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Busca logs de auditoria por tipo de entidade e ID específico.
     * 
     * Retorna histórico completo de mudanças para uma entidade específica.
     * Usado para rastreamento de alterações em projetos, tarefas, etc.
     * 
     * @param entityType tipo da entidade (ex: "Project", "Task")
     * @param entityId ID específico da entidade
     * @return List<AuditLog> logs ordenados por timestamp decrescente
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Busca logs de auditoria por usuário específico.
     * 
     * Permite rastrear todas as ações realizadas por um usuário
     * no sistema. Útil para análise de atividade e auditoria.
     * 
     * @param userId ID do usuário
     * @return List<AuditLog> logs do usuário ordenados por timestamp decrescente
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Busca logs de auditoria por período de tempo.
     * 
     * Permite análise de atividade em períodos específicos.
     * Útil para relatórios mensais, análise de uso, etc.
     * 
     * @param start data/hora de início (inclusive)
     * @param end data/hora de fim (inclusive)
     * @return List<AuditLog> logs no período ordenados por timestamp decrescente
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Busca logs de auditoria por tipo de operação.
     * 
     * Filtra logs por operação específica (CREATE, UPDATE, DELETE).
     * Usado para análise de padrões de uso e debug.
     * 
     * @param operation tipo da operação
     * @return List<AuditLog> logs da operação ordenados por timestamp decrescente
     */
    List<AuditLog> findByOperationOrderByTimestampDesc(String operation);

    /**
     * Busca logs com paginação por entidade e ID.
     * 
     * Versão paginada da busca por entidade específica.
     * Essencial para históricos longos de entidades muito editadas.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @param pageable configuração de paginação
     * @return Page<AuditLog> página de logs
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Query customizada para relatórios de auditoria com filtros múltiplos.
     * 
     * Método mais flexível que permite combinação de filtros opcionais.
     * Todos os parâmetros são opcionais (podem ser null) para máxima
     * flexibilidade na geração de relatórios.
     * 
     * Suporta filtros por:
     * - Tipo de entidade
     * - Usuário que realizou a operação
     * - Tipo de operação (CREATE, UPDATE, DELETE)
     * - Período de tempo
     * 
     * @param entityType tipo da entidade (opcional)
     * @param userId ID do usuário (opcional)
     * @param operation tipo da operação (opcional)
     * @param startDate data de início (opcional)
     * @param endDate data de fim (opcional)
     * @param pageable configuração de paginação
     * @return Page<AuditLog> página de logs filtrados
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:operation IS NULL OR a.operation = :operation) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAuditLogsByFilters(
        @Param("entityType") String entityType,
        @Param("userId") Long userId,
        @Param("operation") String operation,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
} 