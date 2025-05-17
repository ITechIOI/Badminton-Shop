package com.example.orderservice.modules.OrderDetails.repository;

import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.modules.OrderDetails.dto.output.MonthlyRevenueDto;
import com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

@Service
public interface OrderDetailRepository extends JpaRepository<OrderDetails, Long> {
    @Query("SELECT u FROM OrderDetails u WHERE u.id = :id AND u.deletedAt IS NULL")
    public Optional<OrderDetails> findOneById(Long id);

    @Query("SELECT u FROM OrderDetails u WHERE u.deletedAt IS NULL")
    public Page<OrderDetails> findAllOrderDetails(Pageable pageable);

    @Query("SELECT u FROM OrderDetails u WHERE u.order.id = :orderId AND u.deletedAt IS NULL")
    public Page<OrderDetails> findDetailsByOrderId(Long orderId, Pageable pageable);

    @Query("SELECT u FROM OrderDetails u WHERE u.order.id = :orderId AND u.deletedAt IS NULL")
    public List<OrderDetails> findOrderDetailsByOrderIdForService(Long orderId);

    @Query("SELECT u FROM OrderDetails u WHERE u.proudctId = :productId AND u.deletedAt IS NULL")
    public Page<OrderDetails> findOrderDetailsByProductId(Long productId, Pageable pageable);

    // Tìm kiếm top n sản phẩm bán chạy nhất
    @Query("""
    SELECT new com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto(
        u.proudctId, SUM(u.quantity)
    )
    FROM OrderDetails u
    WHERE u.deletedAt IS NULL
    GROUP BY u.proudctId
    ORDER BY SUM(u.quantity) DESC
""")
    Page<RawTopProductDto> findTopSellingProductsAllTime(Pageable pageable);

    @Query("""
        SELECT new com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto(
            u.proudctId, SUM(u.quantity)
        )
        FROM OrderDetails u
        WHERE u.deletedAt IS NULL AND FUNCTION('YEAR', u.createdAt) = :year
        GROUP BY u.proudctId
        ORDER BY SUM(u.quantity) DESC
    """)
    Page<RawTopProductDto> findTopSellingProductsByYear(@Param("year") int year, Pageable pageable);

    @Query("""
        SELECT new com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto(
            u.proudctId, SUM(u.quantity)
        )
        FROM OrderDetails u
        WHERE u.deletedAt IS NULL AND FUNCTION('YEAR', u.createdAt) = :year AND FUNCTION('MONTH', u.createdAt) = :month
        GROUP BY u.proudctId
        ORDER BY SUM(u.quantity) DESC
    """)
    Page<RawTopProductDto> findTopSellingProductsByMonth(@Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query("""
        SELECT new com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto(
            u.proudctId, SUM(u.quantity)
        )
        FROM OrderDetails u
        WHERE u.deletedAt IS NULL AND FUNCTION('YEAR', u.createdAt) = :year AND FUNCTION('MONTH', u.createdAt) = :month AND FUNCTION('DAY', u.createdAt) = :day
        GROUP BY u.proudctId
        ORDER BY SUM(u.quantity) DESC
    """)
    Page<RawTopProductDto> findTopSellingProductsByDay(@Param("year") int year, @Param("month") int month, @Param("day") int day, Pageable pageable);

    @Query("""
    SELECT SUM(u.price * u.quantity)
    FROM OrderDetails u
    WHERE u.deletedAt IS NULL
""")
    Long getRevenueAllTime();

    @Query("""
    SELECT SUM(u.price * u.quantity)
    FROM OrderDetails u
    WHERE u.deletedAt IS NULL
    AND YEAR(u.createdAt) = :year
""")
    Long getRevenueByYear(@Param("year") Integer year);

    @Query("""
    SELECT SUM(u.price * u.quantity)
    FROM OrderDetails u
    WHERE u.deletedAt IS NULL
    AND YEAR(u.createdAt) = :year
    AND MONTH(u.createdAt) = :month
""")
    Long getRevenueByMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("""
    SELECT SUM(u.price * u.quantity)
    FROM OrderDetails u
    WHERE u.deletedAt IS NULL
    AND YEAR(u.createdAt) = :year
    AND MONTH(u.createdAt) = :month
    AND DAY(u.createdAt) = :day
""")
    Long getRevenueByDay(@Param("year") Integer year, @Param("month") Integer month, @Param("day") Integer day);

    @Query("""
    SELECT new com.example.orderservice.modules.OrderDetails.dto.output.MonthlyRevenueDto(
        MONTH(o.createdAt), SUM(od.price * od.quantity)
        )
        FROM OrderDetails od
        JOIN od.order o
        WHERE o.deletedAt IS NULL AND YEAR(o.createdAt) = :year
        GROUP BY MONTH(o.createdAt)
        ORDER BY MONTH(o.createdAt)
    """)
    List<MonthlyRevenueDto> getMonthlyRevenueByYear(@Param("year") int year);



    @Modifying
    @Transactional
    @Query("UPDATE OrderDetails u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    public void softDeleteById(Long id);

}
