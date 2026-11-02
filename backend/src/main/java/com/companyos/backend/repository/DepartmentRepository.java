package com.companyos.backend.repository;

import com.companyos.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Find all departments belonging to the given organization.
     * Checks both organizationId and companyId (legacy alias).
     */
    @Query("SELECT d FROM Department d WHERE d.organizationId = :orgId OR d.companyId = :orgId")
    List<Department> findByOrganizationId(@Param("orgId") Long organizationId);

    /**
     * Find a specific department that belongs to the given organization.
     */
    @Query("SELECT d FROM Department d WHERE d.departmentId = :id AND (d.organizationId = :orgId OR d.companyId = :orgId)")
    Optional<Department> findByDepartmentIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long organizationId);
}
