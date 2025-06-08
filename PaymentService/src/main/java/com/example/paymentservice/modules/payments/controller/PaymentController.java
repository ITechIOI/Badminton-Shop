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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService    paymentService;
    private final PaymentFactory    paymentFactory;
    private final PaymentProducer   paymentProducer;

    @Value("${paypal.SUCCESS_URL}")
    private String successUrl;

    @Value("${paypal.CANCEL_URL}")
    private String cancelUrl;

    @GetMapping("/test")
    public ResponseEntity<PaymentResponse> testing() {
        Payments payment = paymentService.findPaymentById(2L);
        PaymentResponse resp = new PaymentResponse(
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount()
        );
        paymentProducer.sendPaymentConfirmation(resp);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/create/{method}")
    public ResponseEntity<String> createPayment(
            @PathVariable String method,
            @RequestBody CreatePaymentDto dto
    ) {
        double amount = dto.getAmount();
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        try {
            // 1. Khởi tạo processor và tạo Payment trên PayPal
            IPayment processor = paymentFactory.getPaymentMethod(method);
            Payment payment = processor.createPayments(
                    amount, "USD", "paypal", "sale",
                    "Pay by Paypal", cancelUrl, successUrl
            );

            // 2. Lấy link approval_url
            String approvalLink = payment.getLinks().stream()
                    .filter(l -> "approval_url".equals(l.getRel()))
                    .findFirst()
                    .map(Links::getHref)
                    .orElse(null);

            if (approvalLink == null) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Approval URL not found");
            }

            // 3. Lưu Payment entity (giữ method và orderId để callback)
            Payments entity = new Payments();
            entity.setAmount(amount);
            entity.setPaymentMethod(method);
            entity.setTransactionId(payment.getId());
            entity.setStatus("pending");
            entity.setOrderId(dto.getOrderId());
            paymentService.createPayment(entity);

            // 4. Trả về link cho client redirect
            return ResponseEntity.ok(approvalLink);

        } catch (PayPalRESTException e) {
            log.error("PayPalRESTException: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment creation failed");
        }
    }

    @GetMapping("/success")
    public ModelAndView paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID")   String payerId
    ) {
        log.info("Callback /success với paymentId={}, payerId={}", paymentId, payerId);

        // 1. Tìm record đã lưu
        Payments existing = paymentService.findPaymentByTransactionId(paymentId);
        if (existing == null) {
            log.error("Không tìm thấy payment cho transactionId={}", paymentId);
            return new ModelAndView("payment_error");
        }

        // 2. Lấy processor dựa trên method đã lưu
        IPayment processor = paymentFactory.getPaymentMethod(existing.getPaymentMethod());
        if (processor == null) {
            log.error("Không khởi tạo được processor cho method={}", existing.getPaymentMethod());
            return new ModelAndView("payment_error");
        }

        try {
            // 3. Thực thi thanh toán
            Payment payment = processor.executePayment(paymentId, payerId);
            if ("approved".equalsIgnoreCase(payment.getState())) {
                log.info("Payment {} approved", paymentId);

                // 4. Cập nhật trạng thái
                existing.setStatus("approved");
                UpdatePaymentDto updateDto = new UpdatePaymentDto();
                updateDto.setStatus("paid");
                updateDto.setOrderId(existing.getOrderId());
                paymentService.updatePayment(existing.getId(), updateDto);

                return new ModelAndView("payment_success");
            } else {
                log.warn("Payment {} not approved (state={})", paymentId, payment.getState());
            }
        } catch (PayPalRESTException e) {
            log.error("Lỗi executePayment: {}", e.getMessage(), e);
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
    public ResponseEntity<Payments> getPaymentByOrderId(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findPaymentByOrderId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payments> updatePayment(
            @PathVariable Long id,
            @RequestBody UpdatePaymentDto dto
    ) {
        return ResponseEntity.ok(paymentService.updatePayment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Payments>> getAllPayments(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, limit));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Payments> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findPaymentById(id));
    }

    @GetMapping("/service/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentForMicroservices(
            @PathVariable Long orderId
    ) {
        Payments p = paymentService.findPaymentByOrderId(orderId);
        PaymentResponse resp = new PaymentResponse(
                p.getTransactionId(),
                p.getPaymentMethod(),
                p.getOrderId(),
                p.getStatus(),
                p.getAmount()
        );
        log.info("PaymentResponse: {}", resp);
        return ResponseEntity.ok(resp);
    }
}
