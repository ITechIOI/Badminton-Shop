package com.example.orderservice.modules.Discounts.repository;

import com.example.orderservice.models.Discounts;
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
public interface DiscountRepository extends JpaRepository<Discounts, Long> {
    @Query("SELECT u FROM Discounts u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Discounts> findOneById(Long id);

    @Query("SELECT u FROM Discounts u WHERE u.deletedAt IS NULL")
    public Page<Discounts> findAllDiscounts(Pageable pageable);

    @Query("SELECT u FROM Discounts u WHERE u.code = :code AND u.deletedAt IS NULL")
    public Optional<Discounts> findOneByCode(String code);

    @Modifying
    @Transactional
    @Query("UPDATE Discounts u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    public void softDeleteById(Long id);
}
