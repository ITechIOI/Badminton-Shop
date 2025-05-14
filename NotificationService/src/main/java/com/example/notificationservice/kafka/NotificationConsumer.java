package com.example.notificationservice.kafka;

import com.example.notificationservice.modules.feign.Orders.OrderClient;
import com.example.notificationservice.modules.feign.Orders.OrderResponse;
import com.example.notificationservice.modules.feign.Payments.PaymentResponse;
import com.example.notificationservice.modules.feign.Users.UserClient;
import com.example.notificationservice.models.Notifications;
import com.example.notificationservice.modules.dto.CreateNotificationDto;
import com.example.notificationservice.modules.feign.Users.UserResponse;
import com.example.notificationservice.modules.service.NotificationService;
import com.example.notificationservice.utils.NotificationResponseKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final NotificationService notificationService;
    private final OrderClient orderClient;

    @KafkaListener(topics = "inventory-success-events", groupId = "productGroup")
    public void consumeOrderSuccess (PaymentResponse productResponse) {
        System.out.println("Inventory success: {}" + productResponse);
        if (productResponse == null) {
            Notifications notifications = new Notifications();
            notifications.setOrderId(null);
            // Người dùng có id = 1 mặc định là admin
            notifications.setUserId(1L);
            notifications.setTitle("Order Failed");
            notifications.setContent("Order failed to process. Please check the inventory and try again.");
        } else {
            OrderResponse orderResponse = orderClient.getOrderById(productResponse.getOrderId()).getBody();
            CreateNotificationDto newNotification = new CreateNotificationDto();
            newNotification.setTitle("Order Placed Successfully! Complete Payment Now");
            String content = "Thank you for shopping at my badminton shop! Your order has been confirmed. Please complete your payment so we can process your order as soon as possible.";
            newNotification.setContent(content);
            newNotification.setType("email");
            newNotification.setUserId(orderResponse.userId());
            newNotification.setOrderId(orderResponse.id());
            Notifications notification = notificationService.createNotification(newNotification);
        }
    }
//
//    @KafkaListener(topics = "inventory-failed-events", groupId = "orderGroup")
//    public void consumeOrderFailed (OrderResponse orderResponse) {
//        log.info("Order failed: {}", orderResponse);
//    }

   // @KafkaListener(topics = "order-topic", groupId = "notification-group")
   @KafkaListener(topics = "order-topic")
    public OrderResponse consumeOrderSuccess (OrderResponse orderResponse) {
       CreateNotificationDto newNotification = new CreateNotificationDto();
       newNotification.setTitle("Order Placed Successfully! Complete Payment Now");
       String content = "Thank you for shopping at [Store Name]! Your order has been confirmed. Please complete your payment so we can process your order as soon as possible.";
       newNotification.setContent(content);
       newNotification.setType("push");
       newNotification.setUserId(orderResponse.userId());
       // Chú ý cập nhật id cho đơn hàng ở đây
       newNotification.setOrderId(orderResponse.id());
         Notifications notification = notificationService.createNotification(newNotification);
            if (notification == null) {
                throw new IllegalArgumentException("Notification failed to send");
            }
        System.out.println("Consumed order success: {}" + orderResponse);
        return orderResponse;
    }

    @KafkaListener(topics = "payment-topic")
    public PaymentResponse consumePaymentSuccess (PaymentResponse paymentResponse) {
        OrderResponse orderResponse = orderClient.getOrderById(paymentResponse.getOrderId()).getBody();
        CreateNotificationDto newNotification = new CreateNotificationDto();
        newNotification.setTitle("Payment Success");
        newNotification.setType("email");
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Payment Confirmation</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            background: #fff;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\n" +
                "            margin: auto;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background: #4CAF50;\n" +
                "            color: white;\n" +
                "            padding: 15px;\n" +
                "            text-align: center;\n" +
                "            font-size: 20px;\n" +
                "            border-radius: 8px 8px 0 0;\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .order-details {\n" +
                "            background: #f9f9f9;\n" +
                "            padding: 10px;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            font-size: 14px;\n" +
                "            color: #666;\n" +
                "        }\n" +
                "        a {\n" +
                "            color: #4CAF50;\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "        Payment Confirmation for Order "+ paymentResponse.getTransactionId() +"\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "        <p>Dear Customer,</p>\n" +
                "        <p>We are pleased to inform you that your order has been successfully paid! Below are your order details:</p>\n" +
                "\n" +
                "        <div class=\"order-details\">\n" +
                "            <p><strong>Order ID:</strong>" + paymentResponse.getTransactionId() + "</p>\n" +
                "            <p><strong>Payment Method:</strong> " + paymentResponse.getPaymentMethod() + "</p>\n" +
                "            <p><strong>Total Amount:</strong> " + paymentResponse.getAmount() + "</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <p>Your order is being processed and will be shipped soon.</p>\n" +
                "\n" +
                "        <p>If you have any questions, feel free to contact us at <a href=\"mailto:nguyenloan@gmail.com\">support@[Badminton-Store].com</a> or call our hotline <strong>+84 233 734 7777</strong>.</p>\n" +
                "\n" +
                "        <p>Thank you for shopping at <strong>[Badminton-Store]</strong>!</p>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "        &copy; 2024 [Badminton-Store] >Visit our website</a>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        newNotification.setContent(content);
        newNotification.setUserId(orderResponse.userId());
        newNotification.setOrderId(paymentResponse.getOrderId());
        Notifications notification = notificationService.createNotification(newNotification);
        if (notification == null) {
            throw new IllegalArgumentException("Notification failed to send");
        }
        System.out.println("Consumed order success: {}" + paymentResponse);
        return paymentResponse;
    }
}
