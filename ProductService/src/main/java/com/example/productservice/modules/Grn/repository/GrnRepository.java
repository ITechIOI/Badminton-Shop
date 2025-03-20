package com.example.productservice.modules.Grn.repository;

import com.example.productservice.models.GRN;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface GrnRepository extends JpaRepository<GRN, Long> {

    @Query("SELECT u FROM GRN u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<GRN> findGrnById(Long id);

    @Query("SELECT u FROM GRN u WHERE u.receiptId = :receiptId")
    Optional<GRN> findGrnByReceiptId(String receiptId);

    @Query("SELECT u FROM GRN u WHERE u.supplier.id = :supplierId AND u.deletedAt IS NULL")
    Page<GRN> findGrnBySupplierId(Long supplierId, Pageable pageable);

    @Query("SELECT u FROM GRN u WHERE u.deletedAt IS NULL")
    Page<GRN> findAllGrns(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE GRN u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}
