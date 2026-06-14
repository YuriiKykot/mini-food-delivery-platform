package com.fooddelivery.orderservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.orderservice.controller.OrderController;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.exception.OrderNotFoundException;
import com.fooddelivery.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderServiceImpl orderService;

    @Test
    void createOrder_success_return201() throws Exception{
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .items(new ArrayList<>())
                .total(new BigDecimal("25.98"))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_invalidRequest_returns400() throws Exception{
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(null)
                .items(new ArrayList<>())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_success_returns200() throws Exception{
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .items(new ArrayList<>())
                .total(new BigDecimal("25.98"))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception{
        when(orderService.getOrder(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }
}
