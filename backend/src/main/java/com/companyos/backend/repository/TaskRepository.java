package com.companyos.backend.repository;

import com.companyos.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOrganizationId(Long organizationId);
    Optional<Task> findByTaskIdAndOrganizationId(Long taskId, Long organizationId);
    List<Task> findByProjectId(Long projectId);
    List<Task> findByOrganizationIdAndAssigneeId(Long organizationId, Long assigneeId);
    List<Task> findByOrganizationIdAndStatus(Long organizationId, String status);

    long countByOrganizationId(Long organizationId);
    long countByOrganizationIdAndStatus(Long organizationId, String status);

    @Query("SELECT t FROM Task t WHERE t.organizationId = :orgId AND t.status != 'COMPLETED' AND t.dueDate < :today")
    List<Task> findOverdueTasks(@Param("orgId") Long orgId, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.organizationId = :orgId AND t.status = 'BLOCKED'")
    List<Task> findBlockedTasks(@Param("orgId") Long orgId);
}
