package com.companyos.backend.repository;

import com.companyos.backend.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProjectId(Long projectId);
    List<Milestone> findByOrganizationId(Long organizationId);
    Optional<Milestone> findByMilestoneIdAndOrganizationId(Long milestoneId, Long organizationId);
}
