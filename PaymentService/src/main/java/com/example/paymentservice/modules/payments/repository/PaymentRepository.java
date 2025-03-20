package com.example.paymentservice.modules.payments.repository;


import com.example.paymentservice.models.Payments;
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
public interface PaymentRepository extends JpaRepository<Payments, Long> {

    @Query("SELECT u FROM Payments u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Payments> findOneById(Long id);

    @Query("SELECT u FROM Payments u WHERE u.deletedAt IS NULL")
    public Page<Payments> findAllPayment(Pageable pageable);

    @Query("SELECT u FROM Payments u WHERE u.orderId = :orderId AND u.deletedAt IS NULL")
    public List<Payments> findByOrderId(Long orderId);

    @Query("SELECT u FROM Payments u WHERE u.status = :status AND u.deletedAt IS NULL")
    public Page<Payments> findByStatus(String status, Pageable pageable);

    @Query("SELECT u FROM Payments u WHERE u.transactionId = :transactionId AND u.deletedAt IS NULL")
    public Payments findByTransactionId(String transactionId);

    @Modifying
    @Transactional
    @Query("UPDATE Payments u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    public void softDeleteById(Long id);

}
