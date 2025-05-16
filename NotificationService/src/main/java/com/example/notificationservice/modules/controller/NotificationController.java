package com.example.notificationservice.modules.controller;

import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.dto.PushNotificationDto;
import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.modules.feign.Users.UserResponse;
import com.example.notificationservice.modules.service.NotificationService;
import com.example.notificationservice.modules.service.NotificationStrategy.EmailNotification;
import com.example.notificationservice.modules.service.NotificationStrategy.PushNotification;
import com.example.notificationservice.utils.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final PushNotification pushNotification;

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

    @PostMapping("send")
    public ResponseEntity<String> sendNotification(@RequestBody PushNotificationDto createNotificationDto) {
        try {
            // Build JSON dạng yêu cầu
            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> keys = Map.of(
                    "p256dh", createNotificationDto.getP256dh(),
                    "auth", createNotificationDto.getAuth()
            );

            Map<String, Object> subscription = Map.of(
                    "endpoint", createNotificationDto.getEndpoint(),
                    "keys", keys
            );

            String jsonString = mapper.writeValueAsString(subscription);

            pushNotification.sendNotification(jsonString, "Hello", "Great content");

            return ResponseEntity.ok("Gửi thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }


//    // API này cho phép gửi thông báo đẩy đến client
//    @PostMapping("/send")
//    public ResponseEntity<?> send(@AuthenticationPrincipal Jwt jwt, @RequestBody PushNotificationDto dto) {
//        // Lấy thông tin người dùng từ JWT
//        String keycloakId = jwt.getClaim("sub");
//
//        UserResponse user = userClient.getUserByKeycloakId(keycloakId).getBody();
//        if (user == null) {
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        // Lấy thông tin người dùng từ JWT
//        Long userId = user.id();
//
//        String endpoint = dto.getEndpoint();
//        String auth = dto.getAuth();
//        String p256dh = dto.getP256dh();
//        System.out.println("Subscription: " + dto.getEndpoint() + " " + dto.getAuth() + " " + dto.getP256dh());
//        return ResponseEntity.ok("Received subscription");
//    }

}
