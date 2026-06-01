package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        log.info("POST /orders - customerId = {}", createOrderRequest.getCustomerId());
        OrderResponse response = orderService.createOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId){
        log.info("GET /orders/{}", orderId);
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrderByCustomer(@PathVariable Long customerId){
        log.info("GET /orders/customer/{}", customerId);
        return ResponseEntity.ok(orderService.getOrderByCustomer(customerId));
    }
}
