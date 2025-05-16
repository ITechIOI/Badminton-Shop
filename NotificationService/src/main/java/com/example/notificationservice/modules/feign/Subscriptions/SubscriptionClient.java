package com.example.notificationservice.modules.feign.Subscriptions;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService", url = "${application.config.user-url}" + "subscriptions", contextId = "subscriptionClient")
public interface SubscriptionClient {
    @GetMapping("/userId/{userId}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionByUserId(@PathVariable("userId") Long userId);
}


