package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.MaterialRequestItem;

@Repository
public interface MaterialRequestItemRepository extends JpaRepository<MaterialRequestItem, Long> {
    
    List<MaterialRequestItem> findByMaterialRequestId(Long materialRequestId);
    
    @Query("SELECT mri FROM MaterialRequestItem mri " +
           "JOIN FETCH mri.material m " +
           "WHERE mri.materialRequest.id = :materialRequestId")
    List<MaterialRequestItem> findByMaterialRequestIdWithMaterial(@Param("materialRequestId") Long materialRequestId);
} 