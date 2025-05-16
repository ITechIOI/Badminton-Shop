package com.example.userservice.modules.Subscriptions.controller;


import com.example.userservice.models.Subscriptions;
import com.example.userservice.models.Users;
import com.example.userservice.modules.Subscriptions.dto.CreateSubscriptionDto;
import com.example.userservice.modules.Subscriptions.dto.PushNotificationDto;
import com.example.userservice.modules.Subscriptions.dto.SubscriptionResponse;
import com.example.userservice.modules.Subscriptions.dto.UpdateSubscriptionDto;
import com.example.userservice.modules.Subscriptions.service.SubscriptionService;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @PostMapping("new")
    public ResponseEntity<Subscriptions> createSubscription(@RequestBody CreateSubscriptionDto createSubscriptionDto) {
        System.out.println("Create new subscription: " + createSubscriptionDto.toString());
        return ResponseEntity.ok(subscriptionService.createSubscription(createSubscriptionDto));
    }

    @PostMapping("/subscript")
    public ResponseEntity<Void> send(@AuthenticationPrincipal Jwt jwt, @RequestBody PushNotificationDto dto) {
        String keycloakId = jwt.getClaim("sub");

        Users user = userService.findRawByKeycloakId(keycloakId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        Long userId = user.getId();

        String endpoint = dto.getEndpoint();
        String auth = dto.getAuth();
        String p256dh = dto.getP256dh();

        CreateSubscriptionDto createSubscriptionDto = new CreateSubscriptionDto();
        createSubscriptionDto.setEndpoint(endpoint);
        createSubscriptionDto.setAuth(auth);
        createSubscriptionDto.setP256dh(p256dh);
        createSubscriptionDto.setUserId(userId);
        // Lưu subscription vào database
        Subscriptions subscription = subscriptionService.createSubscription(createSubscriptionDto);

        // Gửi thông báo kiểu void như đã nhận rồi mà không có body
        return ResponseEntity.ok().build();
    }

    // Không cho phép cập nhật userId
    @PutMapping("/{id}")
    public ResponseEntity<Subscriptions> updateSubscriptions(
            @PathVariable ("id") Long id,
            @RequestBody UpdateSubscriptionDto updateSubscriptionDto) {
        System.out.println("Update subscription: " + updateSubscriptionDto.toString());
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, updateSubscriptionDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Subscriptions> getSubscriptionById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(subscriptionService.findSubscriptionById(id));
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionByUserId(@PathVariable Long userId) {
        Subscriptions subscriptions = subscriptionService.findSubscriptionByUserId(userId);

        SubscriptionResponse subscriptionResponse = new SubscriptionResponse(
                subscriptions.getEndpoint(),
                subscriptions.getAuth(),
                subscriptions.getP256dh(),
                subscriptions.getUser().getId()
        );

        System.out.println("Subscription response: " + subscriptionResponse.toString());

        return ResponseEntity.ok(subscriptionResponse);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

}
