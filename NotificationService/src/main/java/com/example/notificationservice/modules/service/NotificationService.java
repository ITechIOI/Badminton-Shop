package com.example.notificationservice.modules.service;

import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Orders.OrderResponse;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.modules.feign.Users.UserResponse;
import com.example.notificationservice.modules.repository.NotificationRepository;
import com.example.notificationservice.modules.service.NotificationStrategy.EmailNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.NotificationStrategy;
import com.example.notificationservice.modules.service.NotificationStrategy.PushNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.SMSNotification;
import com.example.notificationservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Setter
@Getter
@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private NotificationStrategy notificationStrategy;
    private final EmailNotification emailStrategy;
    private final PushNotification pushStrategy;
    private final SMSNotification smsStrategy;
    private final UserClient userClient;
    private final OrderClient orderClient;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailNotification emailStrategy,
            PushNotification pushStrategy,
            SMSNotification smsStrategy,
            UserClient userClient,
            OrderClient orderClient
    ) {
        this.notificationRepository = notificationRepository;
        this.emailStrategy = emailStrategy;
        this.pushStrategy = pushStrategy;
        this.smsStrategy = smsStrategy;
        this.userClient = userClient;
        this.orderClient = orderClient;
    }

    public Notifications createNotification(CreateNotificationDto createNotification) {

        UserResponse userResponse = userClient.getUserById(createNotification.getUserId()).getBody();
        System.out.println(userResponse);
        if (userResponse == null) {
            throw new IllegalArgumentException("User not found");
        }
        OrderResponse orderResponse = orderClient.getOrderById(createNotification.getOrderId()).getBody();
        if (orderResponse == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Notifications notification = new Notifications();

        switch (createNotification.getType().toLowerCase()) {
            case "email":
                notificationStrategy = emailStrategy;
                break;
            case "push":
                notificationStrategy = pushStrategy;
                break;
            case "sms":
                notificationStrategy = smsStrategy;
                break;
            default:
                throw new IllegalArgumentException("Invalid notification type");
        }

        notificationStrategy.sendNotification(
                userResponse.email(),
                createNotification.getTitle(),
                createNotification.getContent()
        );

        notification.setContent(createNotification.getContent());
        notification.setTitle(createNotification.getTitle());
        notification.setType(createNotification.getType());
        notification.setUserId(createNotification.getUserId());
        notification.setOrderId(createNotification.getOrderId());

        return notificationRepository.save(notification);
    }

    public PagedResponse<Notifications> getNotificationByUserId(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Notifications> notifications = notificationRepository.findNotificationByUserId(pageable, userId);
        if (notifications.getContent().isEmpty()) {
            throw new IllegalArgumentException("Notification not found");
        }
        return new PagedResponse<>(notifications.getContent(), notifications.getTotalPages(), notifications.getTotalElements());
    }

}
