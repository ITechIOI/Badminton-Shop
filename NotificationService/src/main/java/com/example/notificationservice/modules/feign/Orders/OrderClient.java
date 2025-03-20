package com.example.notificationservice.modules.feign.Orders;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "OrderService", url = "${application.config.order-url}")
public interface OrderClient {
    @GetMapping("/service/id/{id}")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long id) ;
}
