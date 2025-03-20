package com.example.paymentservice.modules.payments.service.PaymentFactory.concrete;

import com.example.paymentservice.modules.payments.service.PaymentFactory.IPayment;
import com.example.paymentservice.utils.Response;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Service;

@Service
public class Zalopay implements IPayment {
    @Override
     public Payment createPayments(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        System.out.println("Zalopay payment");
        return new Payment();
    }

    @Override
    public Payment executePayment(
            String paymentId,
            String payerId
    ) throws PayPalRESTException {
        System.out.println("Zalopay payment");
        return new Payment();
    }
}
