package com.companyos.backend.service.impl;

import com.companyos.backend.entity.Organization;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.OrganizationRepository;
import com.companyos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserRegistrationHelper — saves a new organization and user in an independent transaction.
 */
@Service
public class UserRegistrationHelper {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserRegistrationHelper(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User saveAndCommit(User user) {
        return userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User saveUserWithOrganization(User user, Organization organization) {
        Organization savedOrg = organizationRepository.save(organization);
        user.setOrganizationId(savedOrg.getOrganizationId());
        return userRepository.save(user);
    }
}
