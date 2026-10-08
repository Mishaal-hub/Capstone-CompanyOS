package com.companyos.backend.repository;

import com.companyos.backend.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByOrganizationId(Long organizationId);
    Optional<Goal> findByGoalIdAndOrganizationId(Long goalId, Long organizationId);
    List<Goal> findByOrganizationIdAndOwnerEmployeeId(Long organizationId, Long ownerEmployeeId);
    List<Goal> findByOrganizationIdAndDepartmentId(Long organizationId, Long departmentId);
    long countByOrganizationId(Long organizationId);
}
