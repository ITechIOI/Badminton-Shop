package com.example.paymentservice.modules.payments.service.PaymentFactory;

import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.COD;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Momo;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Paypal;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Zalopay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public abstract class PaymentFactory {
    @Autowired
    private Paypal payPal;

    @Autowired
    private Momo moMo;

    @Autowired
    private COD cod;

    @Autowired
    private Zalopay zalopay;

    public abstract IPayment getPaymentMethod(String method);
}
