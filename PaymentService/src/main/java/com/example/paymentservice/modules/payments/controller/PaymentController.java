package com.example.paymentservice.modules.payments.controller;

import com.example.paymentservice.kafka.PaymentProducer;
import com.example.paymentservice.models.Payments;
import com.example.paymentservice.modules.payments.dto.CreatePaymentDto;
import com.example.paymentservice.modules.payments.dto.PaymentResponse;
import com.example.paymentservice.modules.payments.dto.UpdatePaymentDto;
import com.example.paymentservice.modules.payments.service.PaymentFactory.IPayment;
import com.example.paymentservice.modules.payments.service.PaymentFactory.PaymentFactory;
import com.example.paymentservice.modules.payments.service.PaymentService;
import com.example.paymentservice.utils.PagedResponse;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentFactory paymentFactory;

    private IPayment paymentPaypal;

    private final PaymentProducer paymentProducer;

    private Long orderId;
//    @Autowired
//    private Paypal paypal;

    @Value("${paypal.SUCCESS_URL}")
    private String successUrl;

    @Value("${paypal.CANCEL_URL}")
    private String cancelUrl;

//    public class PaymentProducer {
//        private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;
//
//        public void sendPaymentConfirmation(PaymentResponse paymentResponse) {
//            log.info("Payment confirmation sent: {}", paymentResponse);
//            Message<PaymentResponse> message = MessageBuilder
//                    .withPayload(paymentResponse)
//                    .setHeader(TOPIC, "payment-topic")
//                    .build();
//            kafkaTemplate.send(message);
//        }
//    }

    @GetMapping("/test")
    public ResponseEntity<PaymentResponse> testing() {
        Payments payment = paymentService.findPaymentById(2L);
        PaymentResponse paymentResponse = new PaymentResponse(
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount()
        );
        paymentProducer.sendPaymentConfirmation(paymentResponse);
        return ResponseEntity.ok(paymentResponse);
    }

    @PostMapping("/create/{method}")
    public RedirectView createPayment(
            @PathVariable("method") String method,
            @RequestBody CreatePaymentDto createPaymentDto
    ) {
        orderId = createPaymentDto.getOrderId();
        paymentPaypal =  paymentFactory.getPaymentMethod(method);
        System.out.println("Get amount: " + createPaymentDto.getAmount());
        createPaymentDto.setAmount(createPaymentDto.getAmount());
        createPaymentDto.setOrderId(createPaymentDto.getOrderId());
        try {
            System.out.println("SuccessUrl: " + successUrl);
            System.out.println("CancelUrl: " + cancelUrl);

            double amount = createPaymentDto.getAmount();

            if (amount <= 0) {
                return new RedirectView("/payment/error?message=Invalid amount");
            }

            BigDecimal formattedAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);

            Payment payment = paymentPaypal.createPayments(
                    amount,
                    "USD",
                    "paypal",
                    "sale",
                    "Pay by Paypal",
                    cancelUrl,
                    successUrl
            );
            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {

                    Payments payments = new Payments();

                    System.out.println("PaymentId 1 : " + payment.getId());

                    payments.setAmount(amount);
                    payments.setPaymentMethod(createPaymentDto.getPaymentMethod());
                    payments.setTransactionId(payment.getId());
                    payments.setStatus("pending");
                    payments.setOrderId(createPaymentDto.getOrderId());

                    Payments newPayment = paymentService.createPayment(payments);

                    return new RedirectView(link.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/success")
    public ModelAndView paymentSuccess (
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ) {
        System.out.println("PaymentId: " + paymentId);
        Payments checkPayment = paymentService.findPaymentByTransactionId(paymentId);
        try {
            Payment payment = paymentPaypal.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                checkPayment.setStatus("approved");
                UpdatePaymentDto updatePaymentDto = new UpdatePaymentDto();
                updatePaymentDto.setStatus("paid");
                updatePaymentDto.setOrderId(orderId);
                paymentService.updatePayment(checkPayment.getId(), updatePaymentDto);
                return new ModelAndView("payment_success");
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
            return new ModelAndView("payment_error");
        }
        return new ModelAndView("payment_error");
    }

    @GetMapping("/cancel")
    public ModelAndView paymentCancel() {
        return new ModelAndView("payment_error");
    }

    @GetMapping("/error")
    public ModelAndView paymentError() {
        return new ModelAndView("payment_error");
    }

    @GetMapping("/orderId/{id}")
    public ResponseEntity<Payments> getPaymentByOrderId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.findPaymentByOrderId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payments> updatePayment(@PathVariable Long id, @RequestBody UpdatePaymentDto updatePaymentDto) {
        return ResponseEntity.ok(paymentService.updatePayment(id, updatePaymentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Payments> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<Payments>> getAllPayments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, limit));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Payments> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findPaymentById(id));
    }

    @GetMapping("/service/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByIdForMicroservices(@PathVariable Long orderId) {
        Payments payment = paymentService.findPaymentByOrderId(orderId);
        System.out.println("Payment information: " + payment);
        PaymentResponse paymentResponse = new PaymentResponse(
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount()
        );
        System.out.println("PaymentResponse: " + paymentResponse);
        return ResponseEntity.ok(paymentResponse);
    }

}
