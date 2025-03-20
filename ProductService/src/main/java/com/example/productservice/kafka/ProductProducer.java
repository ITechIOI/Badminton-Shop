package com.example.productservice.kafka;

import com.example.productservice.modules.feign.Payments.PaymentResponse;
import com.example.productservice.utils.NotificationResponseKafka;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Setter
@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductProducer {
    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    // Tại đây có thể config cho cả logic success và failed
    public void sendUpdateInventorySuccess(PaymentResponse productResponse) {
        log.info("Update inventory sent: {}", productResponse);
        Message<PaymentResponse> message = MessageBuilder
                .withPayload( productResponse)
                .setHeader(TOPIC, "inventory-success-events")
                .build();
        kafkaTemplate.send(message);
    }

//    public void sendUpdateInventoryFailed(String response) {
//        log.info("Update inventory failed: {}", response);
//        Message<String> message = MessageBuilder
//                .withPayload(response)
//                .setHeader(TOPIC, "inventory-failed-events")
//                .build();
//        kafkaTemplate.send(message);
//    }
}



//@Setter
//@Getter
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class PaymentProducer {
//    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;
//
//    public void sendPaymentConfirmation(PaymentResponse paymentResponse) {
//        log.info("Payment confirmation sent: {}", paymentResponse);
//        Message<PaymentResponse> message = MessageBuilder
//                .withPayload(paymentResponse)
//                .setHeader(TOPIC, "payment-topic")
//                .build();
//        kafkaTemplate.send(message);
//    }