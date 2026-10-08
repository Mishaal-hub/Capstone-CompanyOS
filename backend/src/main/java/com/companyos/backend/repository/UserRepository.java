package com.companyos.backend.repository;

import com.companyos.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Find a user by their stable Google subject ID ("sub" claim from Google ID token).
     * This is the preferred lookup for returning Google OAuth users.
     */
    Optional<User> findByGoogleId(String googleId);
}