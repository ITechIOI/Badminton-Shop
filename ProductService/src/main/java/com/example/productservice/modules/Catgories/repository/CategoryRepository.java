package com.example.productservice.modules.Catgories.repository;

import com.example.productservice.models.Categories;
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
public interface CategoryRepository extends JpaRepository<Categories, Long> {
    Optional<Categories> findByName(String name);

    @Query("SELECT u FROM Categories u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<Categories> findByOneId(Long id);

    @Query("SELECT u FROM Categories u WHERE u.deletedAt IS NULL")
    Page<Categories> findAllCategories(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Categories u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}
