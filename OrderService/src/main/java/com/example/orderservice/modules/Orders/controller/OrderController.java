package com.example.orderservice.modules.Orders.controller;

import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.Orders.dto.CreateOrderDto;
import com.example.orderservice.modules.Orders.dto.OrderResponse;
import com.example.orderservice.modules.Orders.dto.UpdateOrderDto;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/new")
    public ResponseEntity<Orders> createOrder(@RequestBody CreateOrderDto createOrderDto) {
        return ResponseEntity.ok(orderService.createOrder(createOrderDto));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @GetMapping("/service/id/{id}")
    public ResponseEntity<OrderResponse> getOrderByIdForService(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.findOrderByIdForServices(id));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Orders> getOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Orders>> getAllOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(page, limit));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponse<Orders>> getOrderByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderService.findOrderByUserId(userId, page, limit));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<PagedResponse<Orders>> getOrderByStatus(
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderService.findOrderByStatus(status, page, limit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orders> updateOrder(@PathVariable("id") Long id, @RequestBody UpdateOrderDto updateOrderDto) {
        return ResponseEntity.ok(orderService.updateOrders(id, updateOrderDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Orders> deleteOrder(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/service/user/{orderId}")
    public ResponseEntity<UserResponse> getUserInformationOrderId(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getUserInformationOrderId(orderId));
    }

}
