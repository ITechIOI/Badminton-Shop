package com.example.userservice.modules.Subscriptions.repository;

import com.example.userservice.models.Subscriptions;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscriptions, Long> {
    // Tìm kiếm subscription theo id
    @Query("SELECT s FROM Subscriptions s WHERE s.id = ?1 AND s.deletedAt IS NULL")
    public Optional<Subscriptions> findOneById(Long subscriptionId);

    // Tìm kiếm subscription theo userId
    @Query("SELECT s FROM Subscriptions s WHERE s.user.id = :userId AND s.deletedAt IS NULL")
    public Optional<Subscriptions> findByUserId(Long userId);

    // Lấy danh sách subscription của hệ thống
    @Query("SELECT s FROM Subscriptions s WHERE s.deletedAt IS NULL")
    public Page<Subscriptions> findAllSubscriptions(Pageable pageable);

    // Xóa mềm một subscription
    @Modifying
    @Transactional
    @Query("UPDATE Subscriptions s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void softDeleteByIdSubscription(Long id);
}
