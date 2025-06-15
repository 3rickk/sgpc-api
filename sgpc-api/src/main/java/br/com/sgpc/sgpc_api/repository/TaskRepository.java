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

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedUser LEFT JOIN FETCH t.createdByUser WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId ORDER BY t.priority DESC, t.endDatePlanned ASC")
    List<Task> findByAssignedUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status ORDER BY t.priority DESC, t.endDatePlanned ASC")
    List<Task> findByAssignedUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.endDatePlanned < :date AND t.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.endDatePlanned < :date AND t.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    List<Task> findOverdueTasksByProject(@Param("projectId") Long projectId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    Long countByAssignedUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority = :priority ORDER BY t.createdAt ASC")
    List<Task> findByProjectIdAndPriority(@Param("projectId") Long projectId, @Param("priority") Integer priority);

    @Query("SELECT t FROM Task t WHERE t.startDatePlanned BETWEEN :startDate AND :endDate OR t.endDatePlanned BETWEEN :startDate AND :endDate")
    List<Task> findTasksInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND (t.startDatePlanned BETWEEN :startDate AND :endDate OR t.endDatePlanned BETWEEN :startDate AND :endDate)")
    List<Task> findTasksByProjectInDateRange(@Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByProjectIdAndTitle(@Param("projectId") Long projectId, @Param("title") String title);
} 