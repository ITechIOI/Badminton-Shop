package com.example.paymentservice.kafka;

import com.example.paymentservice.modules.payments.dto.PaymentResponse;
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
public class PaymentProducer {
    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    public void sendPaymentConfirmation(PaymentResponse paymentResponse) {
        log.info("Payment confirmation sent: {}", paymentResponse);
        Message<PaymentResponse> message = MessageBuilder
                .withPayload(paymentResponse)
                .setHeader(TOPIC, "payment-topic")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRollbackOrder(PaymentResponse paymentResponse) {
        log.info("Rollback order sent: {}", paymentResponse);
        Message<PaymentResponse> message = MessageBuilder
                .withPayload(paymentResponse)
                .setHeader(TOPIC, "rollback-order-topic")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRefundSuccess(PaymentResponse paymentResponse) {
        log.info("Refund success sent: {}", paymentResponse);
        Message<PaymentResponse> message = MessageBuilder
                .withPayload(paymentResponse)
                .setHeader(TOPIC, "refund-success-topic")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRefundFailed(PaymentResponse paymentResponse) {
        log.info("Refund failed sent: {}", paymentResponse);
        Message<PaymentResponse> message = MessageBuilder
                .withPayload(paymentResponse)
                .setHeader(TOPIC, "refund-failed-topic")
                .build();
        kafkaTemplate.send(message);
    }
}


