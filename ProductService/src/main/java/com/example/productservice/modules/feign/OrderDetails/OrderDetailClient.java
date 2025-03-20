package com.example.productservice.modules.feign.OrderDetails;

import com.example.productservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "OrderService", url = "${application.config.order-details-url}",configuration = FeignConfig.class)
public interface OrderDetailClient {

    @GetMapping("/service/all/{orderId}")
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails(
            @PathVariable("orderId") Long orderId
    );
}


//@FeignClient(name = "OrderService", url = "${application.config.order-url}")
//public interface OrderClient {
//    @GetMapping("/service/{id}")
//    ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long id);
//}


//@GetMapping("/service/all/{orderId}")
//public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails(
//        @PathVariable("orderId") Long orderId
//) {
//    return ResponseEntity.ok(orderDetailService.getOrderDetailsForService(orderId));
//}
