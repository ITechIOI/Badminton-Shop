package com.example.notificationservice.modules.controller;

import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.modules.service.NotificationService;
import com.example.notificationservice.modules.service.NotificationStrategy.EmailNotification;
import com.example.notificationservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private EmailNotification emailNotification;
    private final UserClient userClient;
    private final OrderClient orderClient;

    @PostMapping("/create")
    public ResponseEntity<Notifications> sendEmail(@RequestBody CreateNotificationDto createNotificationDto) {
        return ResponseEntity.ok(notificationService.createNotification(createNotificationDto));
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<PagedResponse<Notifications>> getNotificationByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(notificationService.getNotificationByUserId(userId, page, limit));
    }

//    @GetMapping("/user")
//    public ResponseEntity<UserResponse> test() {
//        UserResponse user = userClient.getUserById(4L).getBody();
//        return ResponseEntity.ok(user);
//    }
//
//    @GetMapping("/order")
//    public ResponseEntity<OrderResponse> testOrder() {
//        OrderResponse user = orderClient.getOrderById(1L).getBody();
//        return ResponseEntity.ok(user);
//    }
}
