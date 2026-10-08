package com.companyos.backend.repository;

import com.companyos.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByOrganizationId(Long organizationId);
    Optional<Client> findByClientIdAndOrganizationId(Long clientId, Long organizationId);
    long countByOrganizationId(Long organizationId);
    long countByOrganizationIdAndStatus(Long organizationId, String status);
}
