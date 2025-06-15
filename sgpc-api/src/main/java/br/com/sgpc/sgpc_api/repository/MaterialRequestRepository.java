package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.MaterialRequest;
import br.com.sgpc.sgpc_api.enums.RequestStatus;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    
    List<MaterialRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    List<MaterialRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    
    List<MaterialRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "ORDER BY mr.createdAt DESC")
    List<MaterialRequest> findAllWithDetails();
    
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "WHERE mr.id = :id")
    MaterialRequest findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT mr FROM MaterialRequest mr " +
           "JOIN FETCH mr.project p " +
           "JOIN FETCH mr.requester r " +
           "LEFT JOIN FETCH mr.approvedBy a " +
           "WHERE mr.status = :status " +
           "ORDER BY mr.createdAt DESC")
    List<MaterialRequest> findByStatusWithDetails(@Param("status") RequestStatus status);
} 