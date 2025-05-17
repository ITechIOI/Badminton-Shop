package com.example.orderservice.modules.Orders.service;

import com.example.orderservice.kafka.OrderProducer;
import com.example.orderservice.models.Discounts;
import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.Discounts.service.DiscountService;
import com.example.orderservice.modules.OrderDetails.repository.OrderDetailRepository;
import com.example.orderservice.modules.Orders.dto.CreateOrderDto;
import com.example.orderservice.modules.Orders.dto.OrderResponse;
import com.example.orderservice.modules.Orders.dto.UpdateOrderDto;
import com.example.orderservice.modules.Orders.dto.output.OrderByDateDto;
import com.example.orderservice.modules.Orders.repository.OrderRepository;
import com.example.orderservice.modules.feign.Payments.PaymentClient;
import com.example.orderservice.modules.feign.Payments.PaymentResponse;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.NullAwareBeanUtilsBean;
import com.example.orderservice.utils.PagedResponse;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final DiscountService discountService;
    private final UserClient userClient;
    private final OrderProducer orderProducer;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    public OrderResponse cancelOrder(Long id) {
        Orders orders = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        UpdateOrderDto updateOrderDto = new UpdateOrderDto();
        updateOrderDto.setStatus("cancelled");
        Orders result = updateOrders(id, updateOrderDto);

        OrderResponse response = new OrderResponse(
                orders.getId(),
                orders.getTotalPrice(),
                "cancelled",
                orders.getAddress(),
                orders.getPhone(),
                orders.getUserId(),
                orders.getDiscount().getId()
        );

        System.out.println("Order cancelled" + response.toString());

        orderProducer.cancelOrder(response);
        return response;
    }

    public UserResponse getUserInformationOrderId(Long id) {
        try {
            Orders orders = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
            return userClient.getUserById(orders.getUserId()).getBody();
        } catch (FeignException.FeignClientException e) {
            throw new NotFoundException("User not found with ID: " + id);
        }
    }

    public OrderResponse undoCancelOrder(Long id)  {
        Orders orders = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        UpdateOrderDto updateOrderDto = new UpdateOrderDto();
        updateOrderDto.setStatus("pending");
        Orders result = updateOrders(id, updateOrderDto);

        OrderResponse response = new OrderResponse(
                orders.getId(),
                orders.getTotalPrice(),
                "pending",
                orders.getAddress(),
                orders.getPhone(),
                orders.getUserId(),
                orders.getDiscount().getId()
        );

        System.out.println("Undo order cancelled" + response.toString());

        orderProducer.cancelOrder(response);
        return response;
    }

    public Orders createOrder(CreateOrderDto createOrderDto) {
        Discounts discount = discountService.findDiscountById(createOrderDto.getDiscountId());
        try {
            UserResponse userResponse = userClient.getUserById(createOrderDto.getUserId()).getBody();
        } catch (FeignException.FeignClientException e) {
            throw new NotFoundException("User not found with ID: " + createOrderDto.getUserId());
        }
        Orders order = new Orders();
        order.setDiscount(discount);
        order.setUserId(createOrderDto.getUserId());
        order.setTotalPrice(createOrderDto.getTotalPrice());
        order.setAddress(createOrderDto.getAddress());
        order.setPhone(createOrderDto.getPhone());
        order.setStatus(createOrderDto.getStatus());

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getAddress(),
                order.getPhone(),
                order.getUserId(),
                order.getDiscount().getId()
        );

        Orders savedOrder = orderRepository.save(order);
        for (int i = 0; i < createOrderDto.getDetails().size(); i++) {
            OrderDetails item = new OrderDetails();
            item.setOrder(savedOrder);
            ProductResponse productResponse;
            try {
                productResponse = productClient.getProductById(createOrderDto.getDetails().get(i).getProductId()).getBody();
                System.out.println("Product response: " + productResponse);
            } catch (FeignException.FeignClientException e) {
                throw new NotFoundException("Product not found with ID: " + createOrderDto.getDetails().get(i).getProductId());
            }
            item.setProudctId(createOrderDto.getDetails().get(i).getProductId());
            item.setQuantity(createOrderDto.getDetails().get(i).getQuantity());
            item.setPrice(productResponse.price());
            OrderDetails savedOrderDetail = orderDetailRepository.save(item);
            System.out.println("Order detail created: " + savedOrderDetail.toString());
        }

        orderProducer.sendOrderConfirmation(response);


        return savedOrder;
    }

    public OrderResponse findOrderByIdForServices(Long id) {
        System.out.println("Id of order" + id);
        Orders orders = orderRepository.findOneByIdForUser(id);
        if (orders == null) {
            throw new NotFoundException("Order not found");
        }
        OrderResponse response = new OrderResponse(
                orders.getId(),
                orders.getTotalPrice(),
                orders.getStatus(),
                orders.getAddress(),
                orders.getPhone(),
                orders.getUserId(),
                orders.getDiscount().getId()
        );
        return response;
    }

    public Orders findOrderById(Long id) {
        return orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
    }

    public PagedResponse<Orders> findOrderByUserId(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Orders> orders = orderRepository.findOneByUserId(userId, pageable);
        if (orders.getContent().isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        return new PagedResponse<>(
                orders.getContent(),
                orders.getTotalPages(),
                orders.getTotalElements()
        );
    }

    public PagedResponse<Orders> getAllOrders(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Orders> orders = orderRepository.findAllOrders(pageable);
        if (orders.getContent().isEmpty()) {
            throw new NotFoundException("No order found");
        }
        return new PagedResponse<>(orders.getContent(), orders.getTotalPages(), orders.getTotalElements());
    }

    public PagedResponse<Orders> findOrderByStatus(String status, int page, int offset) {
        Pageable pageable = PageRequest.of(page, offset);
        Page<Orders> orders = orderRepository.findOrdersByStatus(status, pageable);
        if (orders.getContent().isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        return new PagedResponse<>(
                orders.getContent(),
                orders.getTotalPages(),
                orders.getTotalElements()
        );
    }

    public PagedResponse<OrderByDateDto> orderStatisticsByTime(Integer year, Integer month, Integer day, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Orders> ordersPage;

        if (year != null && month != null && day != null) {
            ordersPage = orderRepository.findOrdersByYearAndMonthAndDay(year, month, day, pageable);
        } else if (year != null && month != null) {
            ordersPage = orderRepository.findOrdersByYearAndMonth(year, month, pageable);
        } else if (year != null) {
            ordersPage = orderRepository.findOrdersByYear(year, pageable);
        } else {
            ordersPage = orderRepository.findAllOrders(pageable);
        }

        if (ordersPage.isEmpty()) {
            throw new NotFoundException("No top selling products found");
        }
        System.out.println("Payment list: " + ordersPage.getContent().getLast().toString());

        List<OrderByDateDto> orderDetailsPage = ordersPage.getContent().stream()
                .map(order -> {
                    UserResponse userResponse;
                    PaymentResponse paymentResponse;
                    try {
                        userResponse = userClient.getUserById(order.getUserId()).getBody();
                    } catch (Exception e) {
                        throw new NotFoundException("User not found with ID: " + order.getUserId());
                    }
                    try {
                        paymentResponse = paymentClient.getPaymentByIdForMicroservices(order.getUserId()).getBody();
                    } catch (Exception e) {
                        throw new NotFoundException(e.getMessage());
                    }
                    return new OrderByDateDto(
                            order.getId(),
                            order.getAddress(),
                            order.getPhone(),
                            order.getTotalPrice(),
                            paymentResponse.getPaymentMethod(),
                            order.getStatus(),
                            userResponse,
                            order.getCreatedAt()
                    );
                })
                .toList();

        return new PagedResponse<>(
                orderDetailsPage,
                ordersPage.getTotalPages(),
                ordersPage.getTotalElements()
        );
    }

    public Orders updateOrders(Long id, UpdateOrderDto updateOrderDto) {
        Orders order = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        if (updateOrderDto.getDiscountId() != null) {
            Discounts discount = discountService.findDiscountById(updateOrderDto.getDiscountId());
            order.setDiscount(discount);
        }
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(order, updateOrderDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        if (updateOrderDto.getUserId() != null) {
            try {
                UserResponse userResponse = userClient.getUserById(updateOrderDto.getUserId()).getBody();
            } catch (FeignException.FeignClientException e) {
                throw new NotFoundException("User not found with ID: " + updateOrderDto.getUserId());
            }
        }
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Orders order = orderRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        orderRepository.softDeleteById(id);
    }
}
