package com.companyos.backend.repository;

import com.companyos.backend.entity.AIProposedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIProposedActionRepository extends JpaRepository<AIProposedAction, Long> {
    List<AIProposedAction> findByOrganizationIdAndStatus(Long organizationId, String status);
    Optional<AIProposedAction> findByActionIdAndOrganizationId(Long actionId, Long organizationId);
}
