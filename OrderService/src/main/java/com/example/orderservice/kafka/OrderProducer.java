package com.example.orderservice.kafka;

import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.Orders.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {
    private final KafkaTemplate<String, OrderResponse> kafkaTemplate;

    public void sendOrderConfirmation (OrderResponse orderResponse) {
        log.info("Order confirmation sent: {}", orderResponse);

        Message<OrderResponse> message= MessageBuilder
                .withPayload(orderResponse)
                .setHeader(TOPIC, "order-topic")
                .build();
        kafkaTemplate.send(message);
    }

    public void cancelOrder(OrderResponse orderResponse) {
        log.info("Cancel order: {}", orderResponse);
        Message<OrderResponse> message= MessageBuilder
                .withPayload(orderResponse)
                .setHeader(TOPIC, "order-cancel-events")
                .build();
        kafkaTemplate.send(message);
    }
}
