package com.example.orderservice.modules.Reviews.repository;

import com.example.orderservice.models.Reviews;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    @Query("SELECT u FROM Reviews u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Reviews> findOneById(Long id);

//    @Query("SELECT u FROM Reviews u WHERE u.orders.id = :productId AND u.deletedAt IS NULL")
//    public List<Reviews> findReviewByOrderId(Long productId);

    @Query("SELECT u FROM Reviews u WHERE u.orders.id = :orderId AND u.deletedAt IS NULL")
    public Page<Reviews> findReviewByOrderId(Long orderId, Pageable pageable);

    @Query("SELECT u FROM Reviews u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    public Page<Reviews> findReviewByUserId(Long userId, Pageable pageable);

    @Query("SELECT u FROM Reviews u WHERE u.deletedAt IS NULL")
    public Page<Reviews> findAllReview(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Reviews u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    void softDeleteById(Long id);
}
