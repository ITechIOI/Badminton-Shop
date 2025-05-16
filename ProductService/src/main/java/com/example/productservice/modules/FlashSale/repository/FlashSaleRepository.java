package com.example.productservice.modules.FlashSale.repository;

import com.example.productservice.models.Flash_Sale;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public interface FlashSaleRepository extends JpaRepository<Flash_Sale, Long> {

    // Tìm kiếm flash sale theo id
    @Query("SELECT f FROM Flash_Sale f WHERE f.id = ?1 AND f.deletedAt IS NULL")
    Flash_Sale findOneById(Long flashSaleId);

    // Tìm kiếm flash sale theo startTime và endTime
    @Query("SELECT f FROM Flash_Sale f WHERE f.startTime = ?1 AND f.endTime = ?2 AND f.deletedAt IS NULL")
    Flash_Sale findFlashSaleByTime(LocalDateTime startTime, LocalDateTime endTime);

    // Tìm kiếm tất cả flash sale
    @Query("SELECT f FROM Flash_Sale f WHERE f.deletedAt IS NULL")
    Page<Flash_Sale> findAllFlashSales(Pageable pageable);

    // Xóa mềm một flash sale
    @Modifying
    @Transactional
    @Query("UPDATE Flash_Sale f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :id")
    void softDeleteByIdFlashSale(Long id);

}


