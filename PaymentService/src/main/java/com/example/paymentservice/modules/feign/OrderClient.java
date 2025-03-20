package com.example.paymentservice.modules.feign;

import com.example.paymentservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "OrderService", url = "${application.config.order-url}", configuration = FeignConfig.class)
public interface OrderClient {
    @GetMapping("/service/id/{id}")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long id);
}
