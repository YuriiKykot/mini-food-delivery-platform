package com.fooddelivery.orderservice.integration;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.ErrorResponse;
import com.fooddelivery.orderservice.dto.OrderItemRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.repository.CustomerRepository;
import com.fooddelivery.orderservice.repository.ItemRepository;
import com.fooddelivery.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("fooddeelivery_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0")
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.liquibase.contexts", () -> "dev");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp(){
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /orders should create order and publish OrderCreatedEvent")
    void createOrder_should_saveToDatabase_and_publishEvent(){
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(2)
                                .build()
                )).build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/orders",
                request,
                OrderResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCustomerId()).isEqualTo(1L);

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("PaymentCompletedEvent with SUCCESS should update order status to PAID")
    void paymentCompletedEvent_success_should_updateOrderStatus_toPaid() throws InterruptedException{
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(2)
                                .build()
                )).build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/orders",
                request,
                OrderResponse.class
        );

        Long orderId = response.getBody().getId();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(orderId)
                .customerId(1L)
                .amount(new BigDecimal("12.99"))
                .paymentStatus("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("payment-completed", String.valueOf(orderId),event);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Order order = orderRepository.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
                });
    }

    @Test
    @DisplayName("PaymentCompletedEvent with FAILED should update order status to FAILED")
    void paymentCompletedEvent_failed_should_updateOrderStatus_toFailed() throws InterruptedException{
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(1)
                                .build()
                )).build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/orders",
                request,
                OrderResponse.class
        );

        Long orderId = response.getBody().getId();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(orderId)
                .customerId(1L)
                .amount(new BigDecimal("12.99"))
                .paymentStatus("FAILED")
                .processedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("payment-completed", String.valueOf(orderId),event);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Order order = orderRepository.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
                });
    }

    @Test
    @DisplayName("GET /orders/{id} should return order by id")
    void getOrder_should_returnOrder(){
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemId(1L)
                                .quantity(1)
                                .build()
                )).build();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/orders",
                request,
                OrderResponse.class
        );

        Long orderId = response.getBody().getId();

        ResponseEntity<OrderResponse> getResponse = restTemplate.getForEntity(
                "/orders/" + orderId,
                OrderResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("GET /orders/{id} with non-existing id should return 404")
    void getOrder_notFound_should_return404(){
        ResponseEntity<ErrorResponse> getResponse = restTemplate.getForEntity(
                "/orders/99999",
                ErrorResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody().getMessage()).contains("99999");
    }
}
