package com.example.orderservice.kafka;

import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.feign.Payments.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "rollback-order-topic", groupId = "paymentGroup")
    public void consumeOrderSuccess (PaymentResponse paymentResponse) {
        orderService.undoCancelOrder(paymentResponse.getOrderId());
    }
}
