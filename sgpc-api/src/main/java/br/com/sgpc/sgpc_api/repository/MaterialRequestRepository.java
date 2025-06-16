package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.MaterialRequest;
import br.com.sgpc.sgpc_api.enums.RequestStatus;

/**
 * Repositório para gerenciamento de solicitações de materiais.
 * 
 * Este repositório fornece métodos para acessar e manipular dados das solicitações
 * de materiais, incluindo operações de busca por status, projeto, solicitante,
 * e queries otimizadas com fetch join para carregar dados relacionados.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    
    /**
     * Busca solicitações por status, ordenadas por data de criação (mais recentes primeiro).
     * 
     * @param status Status das solicitações a serem buscadas
     * @return Lista de solicitações ordenadas por data de criação decrescente
     */
    List<MaterialRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    /**
     * Busca solicitações de um projeto específico, ordenadas por data de criação.
     * 
     * @param projectId ID do projeto
     * @return Lista de solicitações do projeto ordenadas por data de criação decrescente
     */
    List<MaterialRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    
    /**
     * Busca solicitações feitas por um usuário específico, ordenadas por data de criação.
     * 
     * @param requesterId ID do usuário solicitante
     * @return Lista de solicitações do usuário ordenadas por data de criação decrescente
     */
    List<MaterialRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    /**
     * Busca todas as solicitações com dados completos carregados.
     * 
     * Esta query utiliza JOIN FETCH para carregar os dados do projeto,
     * solicitante e aprovador (quando aplicável), evitando consultas
     * adicionais e melhorando a performance.
     * 
     * @return Lista de todas as solicitações com dados relacionados carregados
     */
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "ORDER BY mr.createdAt DESC")
    List<MaterialRequest> findAllWithDetails();
    
    /**
     * Busca uma solicitação específica com dados completos carregados.
     * 
     * Esta query utiliza JOIN FETCH para carregar os dados do projeto,
     * solicitante e aprovador (quando aplicável) em uma única consulta.
     * 
     * @param id ID da solicitação
     * @return Solicitação com dados relacionados carregados, ou null se não encontrada
     */
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "WHERE mr.id = :id")
    MaterialRequest findByIdWithDetails(@Param("id") Long id);
    
    /**
     * Busca solicitações por status com dados completos carregados.
     * 
     * Esta query combina filtro por status com carregamento otimizado
     * dos dados relacionados usando JOIN FETCH.
     * 
     * @param status Status das solicitações a serem buscadas
     * @return Lista de solicitações com o status especificado e dados relacionados carregados
     */
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "WHERE mr.status = :status " +
           "ORDER BY mr.createdAt DESC")
    List<MaterialRequest> findByStatusWithDetails(@Param("status") RequestStatus status);
} 