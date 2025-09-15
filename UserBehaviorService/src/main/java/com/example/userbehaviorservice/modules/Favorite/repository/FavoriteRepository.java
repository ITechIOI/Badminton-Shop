package com.example.userbehaviorservice.modules.Favorite.repository;

import com.example.userbehaviorservice.models.Favorites;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public interface FavoriteRepository extends JpaRepository<Favorites, Long> {

    @Query("SELECT f FROM Favorites f WHERE f.id = ?1 AND f.deletedAt IS NULL")
    Favorites findOneById(Long id);

    // Lấy danh sách sản phẩm yêu thích của người dùng
    @Query("SELECT f FROM Favorites f WHERE f.userId = :userId AND f.deletedAt IS NULL")
    Page<Favorites> findAllByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Favorites f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = ?1 AND f.deletedAt IS NULL")
    void deleteByUserIdAndProductId(Long id);

    // Tìm yêu thích theo userId và productId chưa bị xóa
    @Query("SELECT f FROM Favorites f WHERE f.userId = :userId AND f.productId = :productId AND f.deletedAt IS NULL")
    Favorites findByUserIdAndProductId(Long userId, Long productId);
}


//@Modifying
//@Transactional
//@Query("UPDATE Reviews u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
//void softDeleteById(Long id);
