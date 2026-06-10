package com.fooddelivery.orderservice.unit.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.exception.CustomerNotFoundException;
import com.fooddelivery.orderservice.exception.ItemNotFoundException;
import com.fooddelivery.orderservice.exception.OrderNotFoundException;
import com.fooddelivery.orderservice.kafka.producer.OrderEventProducer;
import com.fooddelivery.orderservice.model.Customer;
import com.fooddelivery.orderservice.model.Item;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.repository.CustomerRepository;
import com.fooddelivery.orderservice.repository.ItemRepository;
import com.fooddelivery.orderservice.repository.OrderRepository;
import com.fooddelivery.orderservice.service.OrderService;
import com.fooddelivery.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Item item;

    @BeforeEach
    void setUp(){
        customer = Customer.builder()
                .id(1L)
                .name("Yurii Kykot")
                .email("yk@test.com")
                .phoneNumber("123123123")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Kebab")
                .price(new BigDecimal("31.99"))
                .build();
    }

    @Test
    @DisplayName("Create order with success")
    void createOrder_success(){
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("CREATED");

        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).sendOrderCreatedEvent(any(Order.class));
    }

    @Test
    @DisplayName("Create order with not found(null) customer should throw exception")
    void createOrder_customerNotFound_throwsException(){
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(99L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).sendOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Create order with not found(null) item should throw exception")
    void createOrder_itemNotFound_throwsException(){
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(99L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get order with success")
    void getOrder_success(){
        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get order with not found(null) order should throw exception")
    void getOrder_notFound_throwsException(){
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Update order with status success when paid")
    void updateOrderStatus_success_paid(){
        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("25.98"))
                .paymentStatus("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(event);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PAID));
    }

    @Test
    @DisplayName("Update order with status failed status")
    void updateOrderStatus_failed(){
        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("25.98"))
                .paymentStatus("FAILED")
                .processedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(event);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.FAILED));
    }
}
