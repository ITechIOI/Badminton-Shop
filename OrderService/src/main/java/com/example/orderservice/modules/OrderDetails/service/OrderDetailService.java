package com.example.orderservice.modules.OrderDetails.service;

import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.OrderDetailResponse;
import com.example.orderservice.modules.OrderDetails.dto.UpdateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.repository.OrderDetailRepository;
import com.example.orderservice.modules.Orders.repository.OrderRepository;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.NullAwareBeanUtilsBean;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderDetailService implements OrderDetailServiceInterface {
    private final OrderDetailRepository orderRepository;
    private final OrderService orderService;
    private final ProductClient productClient;

    public OrderDetails createDetails(CreateOrderDetailDto createOrderDetailDto) {
        OrderDetails orderDetails = new OrderDetails();
        ProductResponse product = productClient.getProductById(createOrderDetailDto.getProductId()).getBody();
        orderDetails.setProudctId(createOrderDetailDto.getProductId());
        orderDetails.setQuantity(createOrderDetailDto.getQuantity());
        Orders order = orderService.findOrderById(createOrderDetailDto.getOrderId());
        orderDetails.setOrder(order);
        return orderRepository.save(orderDetails);
    }

    @Override
    public PagedResponse<OrderDetails> findDetailsByOrderId(Long orderId, int page, int limit) {
        Orders order = orderService.findOrderById(orderId);
        Pageable pageable = PageRequest.of(page, limit);
        Page<OrderDetails> orderDetails = orderRepository.findDetailsByOrderId(orderId, pageable);
        if (orderDetails.getContent().isEmpty()) {
            throw new NotFoundException("Order Details not found");
        }
        return new PagedResponse<>(
            orderDetails.getContent(),
            orderDetails.getTotalPages(),
            orderDetails.getTotalElements()
        );
    }

    public PagedResponse<OrderDetails> findDetailsByProductId(Long productId, int page, int limit) {
        ProductResponse product = productClient.getProductById(productId).getBody();
        Pageable pageable = PageRequest.of(page, limit);
        Page<OrderDetails> orderDetails = orderRepository.findOrderDetailsByProductId(productId, pageable);
        if (orderDetails.getContent().isEmpty()) {
            throw new NotFoundException("Order Details not found");
        }
        return new PagedResponse<>(
            orderDetails.getContent(),
            orderDetails.getTotalPages(),
            orderDetails.getTotalElements()
        );
    }

    public PagedResponse<OrderDetails> findAllOrderDetails(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<OrderDetails> orderDetails = orderRepository.findAllOrderDetails(pageable);
        if (orderDetails.getContent().isEmpty()) {
            throw new NotFoundException("Order Details not found");
        }
        return new PagedResponse<>(
            orderDetails.getContent(),
            orderDetails.getTotalPages(),
            orderDetails.getTotalElements()
        );
    }

    public OrderDetails findOrderDetailsById(Long id) {
        return orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order Details not found"));
    }


    public List<OrderDetailResponse> getOrderDetailsForService(Long orderId) {
        List<OrderDetails> orderDetails = orderRepository.findOrderDetailsByOrderIdForService(orderId);
        if (orderDetails.isEmpty()) {
            throw new NotFoundException("Order details not found");
        }
        List<OrderDetailResponse> orderDetailResponses = new ArrayList<>();
        for (OrderDetails orderDetailItem : orderDetails) {
            OrderDetailResponse response = new OrderDetailResponse(
                    orderDetailItem.getQuantity(),
                    orderDetailItem.getOrder().getId(),
                    orderDetailItem.getProudctId()
            );
            orderDetailResponses.add(response);
        }
        return orderDetailResponses;
    }

    public OrderDetails updateOrderDetails (Long id, UpdateOrderDetailDto updateOrderDetailDto) {
        OrderDetails orderDetails = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order Details not found"));
        if (updateOrderDetailDto.getOrderId() != null) {
            Orders order = orderService.findOrderById(updateOrderDetailDto.getOrderId());
            orderDetails.setOrder(order);
        }
        if (updateOrderDetailDto.getProductId() != null) {
            ProductResponse product = productClient.getProductById(updateOrderDetailDto.getProductId()).getBody();
        }
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(orderDetails, updateOrderDetailDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        return orderRepository.save(orderDetails);
    }

    public void deleteOrderDetails(Long id) {
        OrderDetails orderDetails = findOrderDetailsById(id);
        orderRepository.softDeleteById(orderDetails.getId());
    }
}
