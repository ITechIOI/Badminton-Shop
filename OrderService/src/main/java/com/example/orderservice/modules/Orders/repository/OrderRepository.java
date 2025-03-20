package com.example.orderservice.modules.Orders.repository;

import com.example.orderservice.models.Orders;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("SELECT u FROM Orders u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Orders> findOneById(Long id);

    @Query("SELECT u FROM Orders u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Orders findOneByIdForUser(Long id);

    @Query("SELECT u FROM Orders u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    public Page<Orders> findOneByUserId(Long userId, Pageable pageable);

    @Query("SELECT u FROM Orders u WHERE u.status = :status AND u.deletedAt IS NULL")
    public Page<Orders> findOrdersByStatus(String status, Pageable pageable);

    @Query("SELECT u FROM Orders u WHERE u.deletedAt IS NULL")
    public Page<Orders> findAllOrders(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Orders u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    void softDeleteById(Long id);

}
