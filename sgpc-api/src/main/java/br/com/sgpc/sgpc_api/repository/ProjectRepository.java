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

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.teamMembers WHERE p.id = :id")
    Optional<Project> findByIdWithTeamMembers(@Param("id") Long id);

    @Query("SELECT p FROM Project p WHERE p.status = :status")
    List<Project> findByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.client = :client")
    List<Project> findByClient(@Param("client") String client);

    @Query("SELECT p FROM Project p WHERE p.name LIKE %:name%")
    List<Project> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Project p WHERE p.endDatePlanned < :date AND p.status NOT IN ('CONCLUIDO', 'CANCELADO')")
    List<Project> findDelayedProjects(@Param("date") LocalDate date);

    @Query("SELECT p FROM Project p JOIN p.teamMembers tm WHERE tm.id = :userId")
    List<Project> findProjectsByTeamMemberId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.startDatePlanned BETWEEN :startDate AND :endDate")
    List<Project> findProjectsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByName(String name);
} 