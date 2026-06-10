package com.fooddelivery.paymentsevice.unit.service;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.paymentsevice.service.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("processPayment should return PaymentCompletedEvent with orderId and customerId")
    void processPayment_should_returnEvent_with_correctOrderIdAndCustomerId(){
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(1L)
                .customerId(1L)
                .total(new BigDecimal("28.48"))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        PaymentCompletedEvent result = paymentService.processPayment(orderCreatedEvent);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("28.48"));
        assertThat(result.getPaymentStatus()).isIn("SUCCESS","FAILED");
        assertThat(result.getProcessedAt()).isNotNull();
    }
}
