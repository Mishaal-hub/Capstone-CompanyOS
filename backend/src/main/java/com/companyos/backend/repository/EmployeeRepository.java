package com.companyos.backend.repository;

import com.companyos.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find all employees belonging to a specific organization.
     * Uses both organizationId and companyId columns (companyId is the legacy alias).
     */
    @Query("SELECT e FROM Employee e WHERE e.organizationId = :orgId OR e.companyId = :orgId")
    List<Employee> findByOrganizationId(@Param("orgId") Long organizationId);

    /**
     * Find a specific employee that belongs to the given organization.
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :id AND (e.organizationId = :orgId OR e.companyId = :orgId)")
    Optional<Employee> findByEmployeeIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long organizationId);

    /**
     * Count employees in a specific organization.
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.organizationId = :orgId OR e.companyId = :orgId")
    long countByOrganizationId(@Param("orgId") Long organizationId);

    /**
     * Check if an employee with the given email exists in the organization.
     */
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.email = :email AND (e.organizationId = :orgId OR e.companyId = :orgId)")
    boolean existsByEmailAndOrganizationId(@Param("email") String email, @Param("orgId") Long organizationId);
}
