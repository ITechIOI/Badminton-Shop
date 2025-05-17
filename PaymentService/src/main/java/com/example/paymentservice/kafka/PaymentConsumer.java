package com.example.paymentservice.kafka;

import com.example.paymentservice.models.Payments;
import com.example.paymentservice.modules.feign.OrderResponse;
import com.example.paymentservice.modules.payments.dto.PaymentResponse;
import com.example.paymentservice.modules.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    private final PaymentProducer paymentProducer;
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-cancel-events", groupId = "orderGroup")
    public void consumePaymentSuccess (OrderResponse orderResponse) {
        System.out.println("Payment confirmation received in payment service: " + orderResponse);

        Payments payments = paymentService.findPaymentByOrderId(orderResponse.id());
//        paymentService.refundPayment(payments.getFirst().getId());

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentMethod(payments.getPaymentMethod());
        paymentResponse.setOrderId(payments.getOrderId());
        paymentResponse.setAmount(payments.getAmount());
        paymentResponse.setStatus("refunded");
        paymentResponse.setTransactionId(payments.getTransactionId());

        System.out.println("Payment created: " + paymentResponse + "\n");

        if (paymentService.refundPayment(payments.getId())) {
            paymentProducer.sendRefundSuccess(paymentResponse);
        } else {
            paymentProducer.sendRollbackOrder(paymentResponse);
        }

    }
}


//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OrderConsumer {
//    @KafkaListener(topics = "order-topic", groupId = "paymentGroup")
//    public void consumeOrderSuccess (PaymentResponse paymentResponse) {
//        System.out.println("Order confirmation received: {}" + paymentResponse);
//    }
//}

