package com.example.orderservice.modules.OrderDetails.repository;

import com.example.orderservice.models.OrderDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderDetailRepository extends JpaRepository<OrderDetails, Long> {
    @Query("SELECT u FROM OrderDetails u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<OrderDetails> findOneById(Long id);

    @Query("SELECT u FROM OrderDetails u WHERE u.deletedAt IS NULL")
    public Page<OrderDetails> findAllOrderDetails(Pageable pageable);

    @Query("SELECT u FROM OrderDetails u WHERE u.order.id = :orderId AND u.deletedAt IS NULL")
    public Page<OrderDetails> findDetailsByOrderId(Long orderId, Pageable pageable);

    @Query("SELECT u FROM OrderDetails u WHERE u.order.id = :orderId AND u.deletedAt IS NULL")
    public List<OrderDetails> findOrderDetailsByOrderIdForService(Long orderId);

    @Query("SELECT u FROM OrderDetails u WHERE u.proudctId = :productId AND u.deletedAt IS NULL")
    public Page<OrderDetails> findOrderDetailsByProductId(Long productId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE OrderDetails u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    public void softDeleteById(Long id);

}
