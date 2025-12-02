package com.example.notificationservice;

import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Orders.OrderResponse;
import com.example.notificationservice.modules.feign.Subscriptions.SubscriptionClient;
import com.example.notificationservice.modules.feign.Subscriptions.SubscriptionResponse;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.modules.feign.Users.UserResponse;
import com.example.notificationservice.modules.repository.NotificationRepository;
import com.example.notificationservice.modules.service.NotificationService;
import com.example.notificationservice.modules.service.NotificationStrategy.EmailNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.PushNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.SMSNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailNotification emailStrategy;
    @Mock
    private PushNotification pushStrategy;
    @Mock
    private SMSNotification smsStrategy;
    @Mock
    private UserClient userClient;
    @Mock
    private OrderClient orderClient;
    @Mock
    private SubscriptionClient subscriptionClient;
    @InjectMocks
    private NotificationService notificationService;

    // Helper method tạo UserResponse mẫu
    private static UserResponse createUserResponse(Long userId) {
        return new UserResponse(userId, "User " + userId, "male", "avatar.png",
                "0123456789", "user" + userId + "@example.com", "user" + userId, "USER");
    }

    // Helper method tạo OrderResponse mẫu
    private static OrderResponse createOrderResponse(Long orderId) {
        return new OrderResponse(orderId, 100000, "completed", "Address", "0123456789", 2L, null);
    }

    // Helper method tạo SubscriptionResponse mẫu
    private static SubscriptionResponse createSubscriptionResponse(Long userId) {
        return new SubscriptionResponse("https://endpoint.example.com", "auth-key", "p256dh-key", userId);
    }

    // Helper method tạo Notifications mẫu
    private static Notifications createNotification(Long id, String title, String content,
            String type, Long userId, Long orderId) {
        Notifications notification = new Notifications();
        notification.setId(id);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        return notification;
    }

    // ==========================================================
    // TEST METHOD: createNotification
    // ==========================================================
    @Nested
    @DisplayName("createNotification")
    class createNotification {
        // Precondition: Server works stably
        // User with id = 2 and order with id = 2 existed in database

        // Test case 01: UTCN01
        // Input: title="notification", content="new notification", type="push",
        // userId=2, orderId=2
        // Expected: Create notification successfully
        // ==========================================================
        @Test
        @DisplayName("UTCN01: type='push', userId=2, orderId=2 ⇒ success")
        void createNotification_UTCN01_pushSuccess() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("push")
                    .userId(2L)
                    .orderId(2L)
                    .build();

            UserResponse user = createUserResponse(2L);
            OrderResponse order = createOrderResponse(2L);
            SubscriptionResponse subscription = createSubscriptionResponse(2L);
            Notifications savedNotification = createNotification(1L, "notification", "new notification", "push", 2L,
                    2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(orderClient.getOrderById(2L)).thenReturn(ResponseEntity.ok(order));
            when(subscriptionClient.getSubscriptionByUserId(2L)).thenReturn(ResponseEntity.ok(subscription));
            doNothing().when(pushStrategy).sendNotification(anyString(), anyString(), anyString());
            when(notificationRepository.save(any(Notifications.class))).thenReturn(savedNotification);

            // Act
            Notifications result = notificationService.createNotification(createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("notification");
            assertThat(result.getContent()).isEqualTo("new notification");
            assertThat(result.getType()).isEqualTo("push");
            assertThat(result.getUserId()).isEqualTo(2L);
            assertThat(result.getOrderId()).isEqualTo(2L);

            verify(userClient).getUserById(2L);
            verify(orderClient).getOrderById(2L);
            verify(subscriptionClient).getSubscriptionByUserId(2L);
            verify(pushStrategy).sendNotification(anyString(), eq("notification"), eq("new notification"));
            verify(notificationRepository).save(any(Notifications.class));
        }

        // Test case 02: UTCN02
        // Input: title="notification", content="new notification", type="push",
        // userId=2, orderId=2
        // Expected: Create notification successfully
        // ==========================================================
        @Test
        @DisplayName("UTCN02: type='push', userId=2, orderId=2 ⇒ success")
        void createNotification_UTCN02_pushSuccess() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("push")
                    .userId(2L)
                    .orderId(2L)
                    .build();

            UserResponse user = createUserResponse(2L);
            OrderResponse order = createOrderResponse(2L);
            SubscriptionResponse subscription = createSubscriptionResponse(2L);
            Notifications savedNotification = createNotification(1L, "notification", "new notification", "push", 2L,
                    2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(orderClient.getOrderById(2L)).thenReturn(ResponseEntity.ok(order));
            when(subscriptionClient.getSubscriptionByUserId(2L)).thenReturn(ResponseEntity.ok(subscription));
            doNothing().when(pushStrategy).sendNotification(anyString(), anyString(), anyString());
            when(notificationRepository.save(any(Notifications.class))).thenReturn(savedNotification);

            // Act
            Notifications result = notificationService.createNotification(createDto);

            // Assert
            assertThat(result).isNotNull();

            verify(userClient).getUserById(2L);
            verify(orderClient).getOrderById(2L);
            verify(subscriptionClient).getSubscriptionByUserId(2L);
            verify(pushStrategy).sendNotification(anyString(), eq("notification"), eq("new notification"));
            verify(notificationRepository).save(any(Notifications.class));
        }

        // Test case 03: UTCN03
        // Input: title="notification", content="new notification", type="push",
        // userId=2, orderId=2
        // Expected: Create notification successfully
        // ==========================================================
        @Test
        @DisplayName("UTCN03: type='push', userId=2, orderId=2 ⇒ success")
        void createNotification_UTCN03_pushSuccess() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("push")
                    .userId(2L)
                    .orderId(2L)
                    .build();

            UserResponse user = createUserResponse(2L);
            OrderResponse order = createOrderResponse(2L);
            SubscriptionResponse subscription = createSubscriptionResponse(2L);
            Notifications savedNotification = createNotification(1L, "notification", "new notification", "push", 2L,
                    2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(orderClient.getOrderById(2L)).thenReturn(ResponseEntity.ok(order));
            when(subscriptionClient.getSubscriptionByUserId(2L)).thenReturn(ResponseEntity.ok(subscription));
            doNothing().when(pushStrategy).sendNotification(anyString(), anyString(), anyString());
            when(notificationRepository.save(any(Notifications.class))).thenReturn(savedNotification);

            // Act
            Notifications result = notificationService.createNotification(createDto);

            // Assert
            assertThat(result).isNotNull();

            verify(userClient).getUserById(2L);
            verify(orderClient).getOrderById(2L);
            verify(subscriptionClient).getSubscriptionByUserId(2L);
            verify(notificationRepository).save(any(Notifications.class));
        }

        // Test case 04: UTCN04
        // Input: title="notification", content="new notification", type="email",
        // userId=2, orderId=2
        // Expected: Create notification successfully with email
        // ==========================================================
        @Test
        @DisplayName("UTCN04: type='email', userId=2, orderId=2 ⇒ success")
        void createNotification_UTCN04_emailSuccess() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("email")
                    .userId(2L)
                    .orderId(2L)
                    .build();

            UserResponse user = createUserResponse(2L);
            OrderResponse order = createOrderResponse(2L);
            Notifications savedNotification = createNotification(1L, "notification", "new notification", "email", 2L,
                    2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(orderClient.getOrderById(2L)).thenReturn(ResponseEntity.ok(order));
            doNothing().when(emailStrategy).sendNotification(anyString(), anyString(), anyString());
            when(notificationRepository.save(any(Notifications.class))).thenReturn(savedNotification);

            // Act
            Notifications result = notificationService.createNotification(createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo("email");

            verify(userClient).getUserById(2L);
            verify(orderClient).getOrderById(2L);
            verify(emailStrategy).sendNotification(eq("user2@example.com"), eq("notification"), eq("new notification"));
            verify(notificationRepository).save(any(Notifications.class));
            verifyNoInteractions(subscriptionClient);
        }

        // Test case 05: UTCN05
        // Input: title="notification", content="new notification", type="push",
        // userId=100, orderId=2
        // Expected: IllegalArgumentException("User not found with id: 100")
        // ==========================================================
        @Test
        @DisplayName("UTCN05: userId=100 (not exists) ⇒ IllegalArgumentException")
        void createNotification_UTCN05_userNotFound() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("push")
                    .userId(100L)
                    .orderId(2L)
                    .build();

            when(userClient.getUserById(100L)).thenThrow(new RuntimeException("User not found"));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.createNotification(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found with id: 100");

            verify(userClient).getUserById(100L);
            verify(orderClient, never()).getOrderById(any());
            verify(notificationRepository, never()).save(any(Notifications.class));
        }

        // Test case 06: UTCN06
        // Input: title="notification", content="new notification", type="push",
        // userId=2, orderId=100
        // Expected: IllegalArgumentException("Order not found with id: 100")
        // ==========================================================
        @Test
        @DisplayName("UTCN06: orderId=100 (not exists) ⇒ IllegalArgumentException")
        void createNotification_UTCN06_orderNotFound() {
            // Arrange
            CreateNotificationDto createDto = CreateNotificationDto.builder()
                    .title("notification")
                    .content("new notification")
                    .type("push")
                    .userId(2L)
                    .orderId(100L)
                    .build();

            UserResponse user = createUserResponse(2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(orderClient.getOrderById(100L)).thenThrow(new RuntimeException("Order not found"));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.createNotification(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order not found with id: 100");

            verify(userClient).getUserById(2L);
            verify(orderClient).getOrderById(100L);
            verify(notificationRepository, never()).save(any(Notifications.class));
        }
    }
}
