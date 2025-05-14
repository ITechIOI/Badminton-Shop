package com.example.orderservice.modules.Reviews.repository;

import com.example.orderservice.models.Reviews;
import com.example.orderservice.modules.Reviews.dto.output.ProductRatingDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    @Query("SELECT u FROM Reviews u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<Reviews> findOneById(Long id);

    // Tìm kiếm đánh giá theo id sản phẩm trong chi tiết đơn đặt hàng
    @Query("""
    SELECT r 
    FROM Reviews r 
    JOIN Orders o ON r.orders.id = o.id 
    JOIN OrderDetails d ON o.id = d.order.id
    WHERE d.proudctId = :productId 
    AND r.deletedAt IS NULL 
""")
    Page<Reviews> findReviewByProductId(Long productId, Pageable pageable);

    // Tìm kiếm các sản phẩm có trung bình trên ngưỡng rating (làm tròn) tự chọn (tham số đầu vào là rating, rating có thể là 1,2,3,4,5)
    @Query("""
        SELECT d.proudctId AS proudctId, AVG(r.rating) as avgRating
        FROM Reviews r
        JOIN Orders o ON r.orders.id = o.id
        JOIN OrderDetails d ON o.id = d.order.id
        WHERE r.deletedAt IS NULL
        GROUP BY d.proudctId
        HAVING AVG(r.rating) >= :rating
    """)
    public List<ProductRatingDto> findProductByRating(Integer rating);

    @Query("SELECT u FROM Reviews u WHERE u.orders.id = :orderId AND u.deletedAt IS NULL")
    public Page<Reviews> findReviewByOrderId(Long orderId, Pageable pageable);

    @Query("SELECT u FROM Reviews u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    public Page<Reviews> findReviewByUserId(Long userId, Pageable pageable);

    @Query("SELECT u FROM Reviews u WHERE u.deletedAt IS NULL")
    public Page<Reviews> findAllReview(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Reviews u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    void softDeleteById(Long id);
}
