package com.example.paymentservice;

import com.example.paymentservice.models.Payments;
import com.example.paymentservice.modules.feign.OrderClient;
import com.example.paymentservice.modules.feign.OrderResponse;
import com.example.paymentservice.modules.payments.dto.UpdatePaymentDto;
import com.example.paymentservice.modules.payments.repository.PaymentRepository;
import com.example.paymentservice.modules.payments.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderClient orderClient;
    @InjectMocks
    private PaymentService paymentService;

    // Helper method tạo Payments mẫu
    private static Payments createPayment(Long id, String transactionId, String paymentMethod,
            Long orderId, String status, Double amount) {
        Payments payment = new Payments();
        payment.setId(id);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod(paymentMethod);
        payment.setOrderId(orderId);
        payment.setStatus(status);
        payment.setAmount(amount);
        return payment;
    }

    // Helper method tạo OrderResponse mẫu
    private static OrderResponse createOrderResponse(Long orderId) {
        return new OrderResponse(orderId, 100000, "completed", "Address", "0123456789", 1L, null);
    }

    // ==========================================================
    // TEST METHOD: updatePayment
    // ==========================================================
    @Nested
    @DisplayName("updatePayment")
    class updatePayment {
        // Precondition: Server work stably
        // transactionId = "mockito-junit", paymentMethod = "paypal", orderId = 2,
        // status = "completed", amount = 100000)

        // Test case 01: UTUP01
        // Input: id=2, transactionId="mockito", paymentMethod="cod", orderId=3,
        // status="pending", amount=30000
        // Expected: Update successfully
        // ==========================================================
        @Test
        @DisplayName("UTUP01: id=2, update all fields ⇒ success")
        void updatePayment_UTUP01_success() {
            // Arrange
            Long id = 2L;
            UpdatePaymentDto updateDto = new UpdatePaymentDto("mockito", "cod", 3L, "pending", 30000.0);
            Payments existingPayment = createPayment(2L, "mockito-junit", "paypal", 2L, "completed", 100000.0);
            OrderResponse order = createOrderResponse(3L);

            when(paymentRepository.findOneById(id)).thenReturn(Optional.of(existingPayment));
            when(orderClient.getOrderById(3L)).thenReturn(ResponseEntity.ok(order));
            when(paymentRepository.save(any(Payments.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Payments result = paymentService.updatePayment(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);

            verify(paymentRepository).findOneById(id);
            verify(orderClient).getOrderById(3L);
            verify(paymentRepository).save(any(Payments.class));
        }

        // Test case 02: UTUP02
        // Input: id=2, transactionId="mockito", paymentMethod="cod", orderId=3,
        // status="pending", amount=30000
        // Expected: Update successfully
        // ==========================================================
        @Test
        @DisplayName("UTUP02: id=2, update all fields ⇒ success")
        void updatePayment_UTUP02_success() {
            // Arrange
            Long id = 2L;
            UpdatePaymentDto updateDto = new UpdatePaymentDto("mockito", "cod", 3L, "pending", 30000.0);
            Payments existingPayment = createPayment(2L, "mockito-junit", "paypal", 2L, "completed", 100000.0);
            OrderResponse order = createOrderResponse(3L);

            when(paymentRepository.findOneById(id)).thenReturn(Optional.of(existingPayment));
            when(orderClient.getOrderById(3L)).thenReturn(ResponseEntity.ok(order));
            when(paymentRepository.save(any(Payments.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Payments result = paymentService.updatePayment(id, updateDto);

            // Assert
            assertThat(result).isNotNull();

            verify(paymentRepository).findOneById(id);
            verify(orderClient).getOrderById(3L);
            verify(paymentRepository).save(any(Payments.class));
        }

        // Test case 03: UTUP03
        // Input: id=2, transactionId="mockito", paymentMethod="cod", orderId=100,
        // status="pending", amount=30000
        // Expected: RuntimeException("Order not found with id: 100")
        // ==========================================================
        @Test
        @DisplayName("UTUP03: id=2, orderId=100 (not exists) ⇒ RuntimeException")
        void updatePayment_UTUP03_orderNotFound() {
            // Arrange
            Long id = 2L;
            UpdatePaymentDto updateDto = new UpdatePaymentDto("mockito", "cod", 100L, "pending", 30000.0);
            Payments existingPayment = createPayment(2L, "mockito-junit", "paypal", 2L, "completed", 100000.0);

            when(paymentRepository.findOneById(id)).thenReturn(Optional.of(existingPayment));
            when(orderClient.getOrderById(100L)).thenThrow(feign.FeignException.FeignClientException.class);

            // Act & Assert
            assertThatThrownBy(() -> paymentService.updatePayment(id, updateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Order not found with id: 100");

            verify(paymentRepository).findOneById(id);
            verify(orderClient).getOrderById(100L);
            verify(paymentRepository, never()).save(any(Payments.class));
        }

        // Test case 04: UTUP04
        // Input: id=100
        // Expected: RuntimeException("Payment not found")
        // ==========================================================
        @Test
        @DisplayName("UTUP04: id=100 (not exists) ⇒ RuntimeException")
        void updatePayment_UTUP04_paymentNotFound() {
            // Arrange
            Long id = 100L;
            UpdatePaymentDto updateDto = new UpdatePaymentDto("mockito", "cod", 3L, "pending", 30000.0);

            when(paymentRepository.findOneById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.updatePayment(id, updateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Payment not found");

            verify(paymentRepository).findOneById(id);
            verify(orderClient, never()).getOrderById(any());
            verify(paymentRepository, never()).save(any(Payments.class));
        }
    }
}
