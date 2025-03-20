package com.example.notificationservice.modules.repository;

import com.example.notificationservice.models.Notifications;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    @Query("SELECT n FROM Notifications n WHERE n.userId = :userId AND n.deletedAt IS NULL")
    public Page<Notifications> findNotificationByUserId(Pageable pageable, Long userId);

    @Query("SELECT n FROM Notifications n WHERE n.orderId = :orderId AND n.deletedAt IS NULL")
    public Notifications findByOrderId(Long orderId);

    @Query("SELECT n FROM Notifications n WHERE n.userId = :userId AND n.orderId = :orderId AND n.deletedAt IS NULL")
    public Notifications findByUserIdAndOrderId(Long userId, Long orderId);

    @Query("SELECT n FROM Notifications n WHERE n.userId = :userId AND n.deletedAt IS NULL")
    public Page<Notifications> findAllNotificationsByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Notifications u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id AND u.deletedAt IS NULL")
    public void softDeleteById(Long id);

   // public Page<Payments> findByStatus(String status, Pageable pageable);
}
