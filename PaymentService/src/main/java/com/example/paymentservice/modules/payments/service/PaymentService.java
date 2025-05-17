package com.example.paymentservice.modules.payments.service;

import com.example.paymentservice.models.Payments;
import com.example.paymentservice.modules.feign.OrderClient;
import com.example.paymentservice.modules.feign.OrderResponse;
import com.example.paymentservice.modules.payments.dto.UpdatePaymentDto;
import com.example.paymentservice.modules.payments.repository.PaymentRepository;
import com.example.paymentservice.utils.NullAwareBeanUtilsBean;
import com.example.paymentservice.utils.PagedResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public Payments createPayment(Payments payments) {
        System.out.println("Id of order" + payments.getOrderId());
        try {
            OrderResponse order = orderClient.getOrderById(payments.getOrderId()).getBody();
        } catch (FeignException.FeignClientException e) {
            throw new RuntimeException("Order not found with id: " + payments.getOrderId());
        }

        return paymentRepository.save(payments);
    }

    public boolean refundPayment(Long id) {
        UpdatePaymentDto updatePaymentDto = new UpdatePaymentDto();
        updatePaymentDto.setStatus("refunded");
        Payments updatedPayment = updatePayment(id, updatePaymentDto);
        return true;
    }

    public Payments findPaymentByTransactionId(String transactionId) {
        Payments payments = paymentRepository.findByTransactionId(transactionId);
        if (payments == null) {
            throw new RuntimeException("Payment not found");
        }
        return payments;
    }

    public Payments updatePayment(Long id, UpdatePaymentDto updatePaymentDto) {
        System.out.println(updatePaymentDto);
        Payments payment = paymentRepository.findOneById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(payment, updatePaymentDto);
        } catch (Exception e) {
            throw new RuntimeException("Error updating payment");
        }
        System.out.println("Order id is not null" + updatePaymentDto.getOrderId());
        if (updatePaymentDto.getOrderId() != null) {
            try {
                OrderResponse order = orderClient.getOrderById(updatePaymentDto.getOrderId()).getBody();
            } catch (FeignException.FeignClientException e) {
                throw new RuntimeException("Order not found with id: " + updatePaymentDto.getOrderId());
            }
            payment.setOrderId(updatePaymentDto.getOrderId());
        }
        System.out.println(payment);
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        Payments payment = paymentRepository.findOneById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        paymentRepository.softDeleteById(id);
    }

    public Payments findPaymentByOrderId(Long orderId) {
        List<Payments> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }
        return payments.getLast();
    }

    public Payments findPaymentById(Long id) {
        return paymentRepository.findOneById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public PagedResponse<Payments> getAllPayments(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Payments> payments = paymentRepository.findAllPayment(pageable);
        if (payments.getContent().isEmpty()) {
            throw new RuntimeException("No payment found");
        }
        return new PagedResponse<>(payments.getContent(), payments.getTotalPages(), payments.getTotalElements());
    }

}
