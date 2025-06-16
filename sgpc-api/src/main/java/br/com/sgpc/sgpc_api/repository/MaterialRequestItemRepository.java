package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.MaterialRequestItem;

/**
 * Repositório para gerenciamento de itens de solicitações de materiais.
 * 
 * Este repositório fornece métodos para acessar e manipular dados dos itens
 * que compõem as solicitações de materiais, incluindo operações de busca
 * otimizadas com fetch join para evitar problemas de N+1.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Repository
public interface MaterialRequestItemRepository extends JpaRepository<MaterialRequestItem, Long> {
    
    /**
     * Busca todos os itens de uma solicitação de material específica.
     * 
     * @param materialRequestId ID da solicitação de material
     * @return Lista de itens da solicitação
     */
    List<MaterialRequestItem> findByMaterialRequestId(Long materialRequestId);
    
    /**
     * Busca todos os itens de uma solicitação de material com dados do material carregados.
     * 
     * Esta query utiliza JOIN FETCH para carregar os dados do material
     * associado a cada item, evitando consultas adicionais (problema N+1).
     * 
     * @param materialRequestId ID da solicitação de material
     * @return Lista de itens da solicitação com dados do material carregados
     */
    @Query("SELECT mri FROM MaterialRequestItem mri " +
           "JOIN FETCH mri.material m " +
           "WHERE mri.materialRequest.id = :materialRequestId")
    List<MaterialRequestItem> findByMaterialRequestIdWithMaterial(@Param("materialRequestId") Long materialRequestId);
} 