package com.example.productservice.modules.FlashSaleDetails.repository;

import com.example.productservice.models.Flash_Sale;
import com.example.productservice.models.Flash_Sale_Details;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FlashSaleDetailRepository extends JpaRepository<Flash_Sale_Details, Long> {

    // Tìm kiếm flash sale details theo flash sale detail
    @Query("SELECT f FROM Flash_Sale_Details f WHERE f.id = :flashSaleDetailId AND f.deletedAt IS NULL")
    Flash_Sale_Details findByFlashSaleDetailId(Long flashSaleDetailId);

    // Tìm kiếm tất cả flash sale id
    @Query("SELECT f FROM Flash_Sale_Details f WHERE f.flashSale.id = :flashSaleId AND f.deletedAt IS NULL")
    List<Flash_Sale_Details> findAllFlashSaleDetailsByFlashSaleId(Long flashSaleId);

    // Tìm kiếm tất cả product id
    @Query("SELECT f FROM Flash_Sale_Details f WHERE f.product.id = :productId AND f.deletedAt IS NULL")
    List<Flash_Sale_Details> findAllFlashSaleDetailsByProductId(Long productId);

    // Tìm tất cả  flash sale
    @Query("SELECT f FROM Flash_Sale_Details f WHERE f.deletedAt IS NULL")
    Page<Flash_Sale_Details> findAllFlashSaleDetails(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Flash_Sale_Details f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :id")
    void softDeleteByIdFlashSaleDetail(Long id);

}

