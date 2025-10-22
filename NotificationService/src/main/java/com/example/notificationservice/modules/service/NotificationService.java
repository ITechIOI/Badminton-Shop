package com.example.notificationservice.modules.service;

import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Orders.OrderResponse;
import com.example.notificationservice.modules.feign.Subscriptions.SubscriptionClient;
import com.example.notificationservice.modules.feign.Subscriptions.SubscriptionResponse;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.modules.feign.Users.UserResponse;
import com.example.notificationservice.modules.repository.NotificationRepository;
import com.example.notificationservice.modules.service.NotificationStrategy.EmailNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.NotificationStrategy;
import com.example.notificationservice.modules.service.NotificationStrategy.PushNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.SMSNotification;
import com.example.notificationservice.utils.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    private final SubscriptionClient subscriptionClient;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailNotification emailStrategy,
            PushNotification pushStrategy,
            SMSNotification smsStrategy,
            UserClient userClient,
            OrderClient orderClient,
            SubscriptionClient subscriptionClient
    ) {
        this.notificationRepository = notificationRepository;
        this.emailStrategy = emailStrategy;
        this.pushStrategy = pushStrategy;
        this.smsStrategy = smsStrategy;
        this.userClient = userClient;
        this.orderClient = orderClient;
        this.subscriptionClient = subscriptionClient;
    }

    public Notifications createNotification(CreateNotificationDto createNotification) {

        UserResponse userResponse;
        try {
            userResponse = userClient.getUserById(createNotification.getUserId()).getBody();
            System.out.println("User found: " + userResponse);
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with id: " + createNotification.getUserId());
        }
        try {
            OrderResponse orderResponse = orderClient.getOrderById(createNotification.getOrderId()).getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Order not found with id: " + createNotification.getOrderId());
        }

        Notifications notification = new Notifications();

        switch (createNotification.getType().toLowerCase()) {
            case "email":
                notificationStrategy = emailStrategy;
                notificationStrategy.sendNotification(
                        userResponse.email(),
                        createNotification.getTitle(),
                        createNotification.getContent()
                );
                break;
            case "push":
                notificationStrategy = pushStrategy;
                SubscriptionResponse subscriptionResponse;
                try {
                    subscriptionResponse = subscriptionClient.getSubscriptionByUserId(createNotification.getUserId()).getBody();
                    System.out.println("User id: " + createNotification.getUserId());
                    System.out.println("Subscription response: " + subscriptionResponse);
                } catch (Exception e) {
                    throw new NotFoundException("Subscription not found with userId: " + createNotification.getUserId());
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();

                    Map<String, String> keys = Map.of(
                            "p256dh", subscriptionResponse.p256dh(),
                            "auth", subscriptionResponse.auth()
                    );

                    Map<String, Object> subscription = Map.of(
                            "endpoint", subscriptionResponse.endpoint(),
                            "keys", keys
                    );

                    String jsonString = mapper.writeValueAsString(subscription);

                    notificationStrategy.sendNotification(
                            jsonString,
                            createNotification.getTitle(),
                            createNotification.getContent()
                    );

                } catch (Exception e) {
                    throw new IllegalArgumentException("Error converting subscription to send push notification: " + e.getMessage());
                }

                break;
            case "sms":
                notificationStrategy = smsStrategy;
                break;
            default:
                throw new IllegalArgumentException("Invalid notification type");
        }



        notification.setContent(createNotification.getContent());
        notification.setTitle(createNotification.getTitle());
        notification.setType(createNotification.getType());
        notification.setUserId(createNotification.getUserId());
        notification.setOrderId(createNotification.getOrderId());

        return notificationRepository.save(notification);
    }

    public PagedResponse<Notifications> getNotificationByUserId(Long userId, int page, int limit) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Notifications> notifications = notificationRepository.findNotificationByUserId(pageable, userId);
        if (notifications.getContent().isEmpty()) {
            throw new IllegalArgumentException("Notification not found");
        }
        return new PagedResponse<>(notifications.getContent(), notifications.getTotalPages(), notifications.getTotalElements());
    }

}
