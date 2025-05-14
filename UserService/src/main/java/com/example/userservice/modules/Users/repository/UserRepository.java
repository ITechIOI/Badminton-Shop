package com.example.userservice.modules.Users.repository;

import com.example.userservice.models.Users;
import com.example.userservice.utils.PagedResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findUserById(Long id);

    @Query("SELECT u FROM Users u WHERE u.id = :id AND u.deletedAt IS NULL")
    Users findOneById(Long id);

    @Query("SELECT u FROM Users u WHERE u.deletedAt IS NULL")
    public Page<Users> findAllUsers(Pageable pageable);

    // Get user by keycloak id
    @Query("SELECT u FROM Users u WHERE u.keycloakId = :keycloakId AND u.deletedAt IS NULL")
    Users findOneByKeycloakId(String keycloakId);

    @Modifying
    @Transactional
    @Query("UPDATE Users u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}

    
