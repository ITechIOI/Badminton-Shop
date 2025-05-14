package com.example.orderservice.modules.OrderDetails.service;

import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.UpdateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.repository.OrderDetailRepository;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.NullAwareBeanUtilsBean;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailServiceInterface {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public PagedResponse<OrderDetails> findDetailsByOrderId(Long orderId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<OrderDetails> orderDetails = orderDetailRepository.findDetailsByOrderId(orderId, pageable);
        if (orderDetails.getContent().isEmpty()) {
            throw new NotFoundException("Order Details not found");
        }
        return new PagedResponse<>(orderDetails.getContent(), orderDetails.getTotalPages(), orderDetails.getTotalElements());
    }

}
