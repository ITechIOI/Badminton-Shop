package com.example.productservice.modules.GrnDetails.repository;

import com.example.productservice.models.GRN_Details;
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
public interface GrnDetailRepository extends JpaRepository<GRN_Details, Long> {

    @Query("SELECT u FROM GRN_Details u WHERE u.grn.id = :id AND u.deletedAt IS NULL")
    Page<GRN_Details> findByGrnId(Long id, Pageable pageable);

    @Query("SELECT u FROM GRN_Details u WHERE u.product.id = :productId AND u.deletedAt IS NULL")
    Page<GRN_Details> findByProductId( Long productId, Pageable pageable);

    @Query("SELECT u FROM GRN_Details u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<GRN_Details> findOneById(Long id);

    @Query("SELECT u FROM GRN_Details u WHERE u.deletedAt IS NULL")
    Page<GRN_Details> findAllGrnDetails(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE GRN_Details u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}
