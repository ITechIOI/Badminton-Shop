package com.example.paymentservice.modules.payments.service.PaymentFactory.concrete;

import com.example.paymentservice.modules.payments.service.PaymentFactory.IPayment;
import com.example.paymentservice.utils.Response;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class Paypal implements IPayment {

    @Autowired
    private final APIContext apiContext;

    @Autowired
    public Paypal(APIContext apiContext) {
        this.apiContext = apiContext;
        System.out.println("Paypal service initialized with APIContext: " + this.apiContext.getClientID() + " " + this.apiContext.getClientSecret());
    }

//    @Override
//    public Response createPayment(double amount) {
//        try {
//            System.out.println("Paypal payment");
//            Amount payAmount = new Amount();
//            payAmount.setCurrency("USD");
//            payAmount.setTotal(String.format("%.2f", amount));
//
//            Transaction transaction = new Transaction();
//            transaction.setAmount(payAmount);
//            transaction.setDescription("Thanh toán bằng PayPal");
//
//            List<Transaction> transactions = new ArrayList<>();
//            transactions.add(transaction);
//
//            Payer payer = new Payer();
//            payer.setPaymentMethod("paypal");
//
//            Payment payment = new Payment();
//            payment.setIntent("sale");
//            payment.setPayer(payer);
//            payment.setTransactions(transactions);
//
//            Payment createdPayment = payment.create(apiContext);
//            return new Response(200, "Thanh toán PayPal thành công! ID giao dịch: " + createdPayment.getId(), null);
//        } catch (PayPalRESTException e) {
//            return new Response(500, "Thanh toán PayPal thất bại: " + e.getMessage(), null);
//        }
//

    public Payment createPayments(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {

        // 1. Định dạng số tiền chính xác
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.US, "%.2f", total)); // Đảm bảo 2 chữ số sau dấu thập phân

        // 2. Tạo giao dịch
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // 3. Cấu hình người thanh toán
        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        // 4. Tạo Payment
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // 5. Cấu hình URL redirect
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // 6. Xử lý lỗi khi tạo Payment
        try {
            return payment.create(apiContext);
        } catch (PayPalRESTException e) {
            System.err.println("Lỗi khi tạo Payment: " + e.getMessage());
            throw new RuntimeException("Lỗi khi tạo thanh toán với PayPal", e); // Bao gồm exception gốc
        }
    }

    public Payment executePayment(
            String paymentId,
            String payerId
    ) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
}