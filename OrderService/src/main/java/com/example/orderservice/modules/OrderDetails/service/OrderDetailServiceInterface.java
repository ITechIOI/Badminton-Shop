package com.example.orderservice.modules.OrderDetails.service;

import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.UpdateOrderDetailDto;
import com.example.orderservice.utils.PagedResponse;

public interface OrderDetailServiceInterface {

    PagedResponse<OrderDetails> findDetailsByOrderId(Long orderId, int page, int limit);

}
