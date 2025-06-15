package br.com.sgpc.sgpc_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Busca serviços por nome (case insensitive)
     */
    List<Service> findByNameContainingIgnoreCase(String name);

    /**
     * Busca serviços ativos
     */
    List<Service> findByIsActiveTrue();

    /**
     * Busca serviços inativos
     */
    List<Service> findByIsActiveFalse();

    /**
     * Busca serviços por unidade de medida
     */
    List<Service> findByUnitOfMeasurement(String unitOfMeasurement);

    /**
     * Busca serviços ativos por unidade de medida
     */
    List<Service> findByUnitOfMeasurementAndIsActiveTrue(String unitOfMeasurement);

    /**
     * Verifica se existe um serviço com o nome especificado
     */
    boolean existsByName(String name);

    /**
     * Verifica se existe um serviço com o nome especificado (diferente do ID fornecido)
     */
    @Query("SELECT COUNT(s) > 0 FROM Service s WHERE s.name = :name AND s.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Busca serviço por nome exato
     */
    Optional<Service> findByName(String name);

    /**
     * Busca serviços ordenados por nome
     */
    @Query("SELECT s FROM Service s ORDER BY s.name ASC")
    List<Service> findAllOrderByName();

    /**
     * Busca serviços ativos ordenados por nome
     */
    @Query("SELECT s FROM Service s WHERE s.isActive = true ORDER BY s.name ASC")
    List<Service> findActiveServicesOrderByName();

    /**
     * Busca serviços por faixa de custo unitário total
     */
    @Query("SELECT s FROM Service s WHERE (s.unitLaborCost + s.unitMaterialCost + s.unitEquipmentCost) BETWEEN :minCost AND :maxCost")
    List<Service> findByTotalUnitCostBetween(@Param("minCost") Double minCost, @Param("maxCost") Double maxCost);

    /**
     * Busca unidades de medida distintas
     */
    @Query("SELECT DISTINCT s.unitOfMeasurement FROM Service s WHERE s.isActive = true ORDER BY s.unitOfMeasurement")
    List<String> findDistinctUnitOfMeasurement();
} 