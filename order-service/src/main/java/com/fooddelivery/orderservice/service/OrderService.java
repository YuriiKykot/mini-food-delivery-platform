package com.fooddelivery.orderservice.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(Long orderId);
    List<OrderResponse> getOrderByCustomer(Long customerId);
    void updateOrderStatus(PaymentCompletedEvent event);
}
