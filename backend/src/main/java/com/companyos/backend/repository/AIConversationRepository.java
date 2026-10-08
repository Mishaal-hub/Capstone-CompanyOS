package com.companyos.backend.repository;

import com.companyos.backend.entity.AIConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {
    List<AIConversation> findByOrganizationIdAndUserIdOrderByUpdatedAtDesc(Long organizationId, Long userId);
    Optional<AIConversation> findByConversationIdAndOrganizationId(Long conversationId, Long organizationId);
}
