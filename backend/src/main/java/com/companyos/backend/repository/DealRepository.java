package com.companyos.backend.repository;

import com.companyos.backend.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    List<Deal> findByOrganizationId(Long organizationId);
    List<Deal> findByOrganizationIdAndStage(Long organizationId, String stage);
    List<Deal> findByClientId(Long clientId);
    Optional<Deal> findByDealIdAndOrganizationId(Long dealId, Long organizationId);
}
