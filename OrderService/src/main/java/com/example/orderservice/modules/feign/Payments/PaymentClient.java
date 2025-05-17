package com.example.orderservice.modules.feign.Payments;


import com.example.orderservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PaymentService", url = "${application.config.payment-url}", configuration = FeignConfig.class)
public interface PaymentClient {
    @GetMapping("/service/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByIdForMicroservices(@PathVariable Long orderId);
}
