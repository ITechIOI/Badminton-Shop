package com.example.productservice.modules.Suppliers.repository;

import com.example.productservice.models.Suppliers;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface SupplierRepository extends JpaRepository<Suppliers, Long> {

    public Optional<Suppliers> findByName(String name);

    @Query("SELECT u FROM Suppliers u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Suppliers> findOneById(Long id);

    @Query("SELECT u FROM Suppliers u WHERE u.deletedAt IS NULL")
    public Page<Suppliers> findAllSuppliers(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Suppliers u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDeleteById(Long id);
}
