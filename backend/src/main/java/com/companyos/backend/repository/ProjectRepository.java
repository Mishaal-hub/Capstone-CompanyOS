package com.companyos.backend.repository;

import com.companyos.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOrganizationId(Long organizationId);
    Optional<Project> findByProjectIdAndOrganizationId(Long projectId, Long organizationId);
    List<Project> findByOrganizationIdAndStatus(Long organizationId, String status);
    List<Project> findByOrganizationIdAndProjectManagerId(Long organizationId, Long projectManagerId);
    long countByOrganizationId(Long organizationId);
    long countByOrganizationIdAndStatus(Long organizationId, String status);
}
