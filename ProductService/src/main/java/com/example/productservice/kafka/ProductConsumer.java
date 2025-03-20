package com.example.productservice.kafka;

import com.example.productservice.models.Products;
import com.example.productservice.modules.Products.dto.UpdateProductDto;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.modules.feign.OrderDetails.OrderDetailClient;
import com.example.productservice.modules.feign.OrderDetails.OrderDetailResponse;
import com.example.productservice.modules.feign.Payments.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductConsumer {
    private final ProductProducer productProducer;
    private final ProductService productService;
    private final OrderDetailClient orderDetailClient;

    @KafkaListener(topics = "refund-success-topic", groupId = "paymentGroup")
    public void consumeOrderSuccess (PaymentResponse message) {
        try {
            List<OrderDetailResponse> orderDetails = orderDetailClient.getAllOrderDetails(message.getOrderId()).getBody();
            for (OrderDetailResponse orderDetail : orderDetails) {
                Integer originalStock = productService.findProductById(orderDetail.productId()).getQuantity() ;
                UpdateProductDto updateProductDto = new UpdateProductDto();
                updateProductDto.setQuantity(originalStock + orderDetail.quantity());
                Products product = productService.updateProduct(orderDetail.productId(), updateProductDto);
                productProducer.sendUpdateInventorySuccess(message);
            }
        } catch (Exception e) {
            productProducer.sendUpdateInventorySuccess(null);
        }
    }
}


//@KafkaListener(topics = "order-cancel-events", groupId = "orderGroup")
//public void consumePaymentSuccess (OrderResponse orderResponse) {
//    log.info("Payment confirmation received: {}", orderResponse);
//}