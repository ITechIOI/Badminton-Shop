package com.example.productservice.modules.Products.repository;

import com.example.productservice.models.Products;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductRepository extends JpaRepository<Products, Long> {

    public Optional<Products> findByName(String name);

    @Query("SELECT u FROM Products u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Products> findOneById(Long id);

    @Query("SELECT u FROM Products u WHERE u.deletedAt IS NULL")
    public Page<Products> findAllProducts(Pageable pageable);

    @Query("SELECT u FROM Products u WHERE u.category.id = :categoryId AND u.deletedAt IS NULL")
    public Page<Products> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT u FROM Products u WHERE u.brand = :brand AND u.deletedAt IS NULL")
    public Page<Products> findProductsByBrand(String brand, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Products u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}
