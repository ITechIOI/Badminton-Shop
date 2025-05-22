package com.example.userbehaviorservice.modules.Carts.repository;

import com.example.userbehaviorservice.models.Carts;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CartRepository extends JpaRepository<Carts, Long> {
    @Query("SELECT u FROM Carts u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Carts> findOneById(Long id);

    @Query("SELECT u FROM Carts u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    public Page<Carts> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT u FROM Carts u WHERE u.deletedAt IS NULL")
    public Page<Carts> findAllCart(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Carts u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    void softDeleteById(Long id);
}
