package com.example.orderservice.modules.OrderDetails.controller;

import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.output.MonthlyRevenueDto;
import com.example.orderservice.modules.OrderDetails.dto.UpdateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.output.TopProductDto;
import com.example.orderservice.modules.OrderDetails.service.OrderDetailService;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/order-details")
@AllArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    @PostMapping("/new")
    public ResponseEntity<OrderDetails> createOrderDetail(@RequestBody CreateOrderDetailDto createOrderDetailDto) {
        return ResponseEntity.ok(orderDetailService.createDetails(createOrderDetailDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<OrderDetails> getOrderDetailById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderDetailService.findOrderDetailsById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PagedResponse<OrderDetails>> getOrderDetailByOrderId(
            @PathVariable("orderId") Long orderId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderDetailService.findDetailsByOrderId(orderId, page, limit));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<OrderDetails>> getAllOrderDetails(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderDetailService.findAllOrderDetails(page, limit));
    }

    @GetMapping("/service/all/{orderId}")
    public ResponseEntity<List<UpdateOrderDetailDto.OrderDetailResponse>> getAllOrderDetails(
            @PathVariable("orderId") Long orderId
    ) {
        return ResponseEntity.ok(orderDetailService.getOrderDetailsForService(orderId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<OrderDetails>> getOrderDetailByProductId(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(orderDetailService.findDetailsByProductId(productId, page, limit));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<PagedResponse<TopProductDto>> getBestSellingProducts(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "day", required = false) Integer day,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                orderDetailService.findTopSellingProducts(page, limit, year, month, day)
        );
    }

    @GetMapping("/revenue")
    public ResponseEntity<Long> getRevenue(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "day", required = false) Integer day
    ) {
        return ResponseEntity.ok(orderDetailService.getRevenue(year, month, day));
    }

    @GetMapping("/visualize-revenue/{year}")
    public List<MonthlyRevenueDto> visualizeRevenueByYear(
            @PathVariable("year") Integer year
    ) {
        return orderDetailService.visualizeRevenueByYear(year);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetails> updateOrderDetail(@PathVariable("id") Long id, @RequestBody UpdateOrderDetailDto updateOrderDetailDto) {
        return ResponseEntity.ok(orderDetailService.updateOrderDetails(id, updateOrderDetailDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderDetails> deleteOrderDetail(@PathVariable("id") Long id) {
        orderDetailService.deleteOrderDetails(id);
        return ResponseEntity.ok(null);
    }
}
