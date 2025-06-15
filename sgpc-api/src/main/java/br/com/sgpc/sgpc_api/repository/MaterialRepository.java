package br.com.sgpc.sgpc_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    boolean existsByName(String name);
    
    List<Material> findByIsActiveTrue();
    
    List<Material> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    List<Material> findBySupplierContainingIgnoreCaseAndIsActiveTrue(String supplier);
    
    @Query("SELECT m FROM Material m WHERE m.currentStock < m.minimumStock AND m.isActive = true")
    List<Material> findMaterialsBelowMinimumStock();
    
    Optional<Material> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT m FROM Material m WHERE m.isActive = true ORDER BY m.name")
    List<Material> findAllActiveMaterialsOrderedByName();
} 