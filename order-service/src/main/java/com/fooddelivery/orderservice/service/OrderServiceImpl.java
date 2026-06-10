package com.fooddelivery.orderservice.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.exception.CustomerNotFoundException;
import com.fooddelivery.orderservice.exception.ItemNotFoundException;
import com.fooddelivery.orderservice.exception.OrderNotFoundException;
import com.fooddelivery.orderservice.kafka.producer.OrderEventProducer;
import com.fooddelivery.orderservice.model.*;
import com.fooddelivery.orderservice.repository.CustomerRepository;
import com.fooddelivery.orderservice.repository.ItemRepository;
import com.fooddelivery.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private ItemRepository itemRepository;
    private OrderEventProducer orderEventProducer;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository,
                            ItemRepository itemRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    Item item = itemRepository.findById(itemRequest.getItemId())
                            .orElseThrow(() -> new ItemNotFoundException(itemRequest.getItemId()));

                    return OrderItem.builder()
                            .itemId(item.getId())
                            .itemName(item.getName())
                            .itemPrice(item.getPrice())
                            .quantity(itemRequest.getQuantity())
                            .build();
                }).toList();

        Order order = Order.builder()
                .customer(customer)
                .items(orderItems)
                .status(OrderStatus.CREATED)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        log.info("Order created: id = {}, customerId = {}, total = {}",
               saved.getId(), customer.getId(), order.getTotal());

        orderEventProducer.sendOrderCreatedEvent(order);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toResponse(order);
    }

    @Override
    @Transactional
    public List<OrderResponse> getOrderByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .itemId(i.getItemId())
                        .itemName(i.getItemName())
                        .itemPrice(i.getItemPrice())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .items(itemResponses)
                .total(order.getTotal())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateOrderStatus(PaymentCompletedEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

        if("SUCCESS".equals(event.getPaymentStatus())){
            order.setStatus(OrderStatus.PAID);
        }else{
            order.setStatus(OrderStatus.FAILED);
        }

        orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getId(), order.getStatus());
    }
}
