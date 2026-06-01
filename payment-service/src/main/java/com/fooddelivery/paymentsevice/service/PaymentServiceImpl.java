package com.fooddelivery.paymentsevice.service;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService{

    @Override
    public PaymentCompletedEvent processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for orderId = {}, amount = {}",
                event.getOrderId(), event.getTotal());

        boolean payment = simulatePayment(event.getTotal());

        String paymentStatus = payment ? "SUCCESS" : "FAILED";

        log.info("Payment {} for orderId = {}",
                paymentStatus, event.getOrderId());

        return PaymentCompletedEvent.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotal())
                .paymentStatus(paymentStatus)
                .processedAt(LocalDateTime.now())
                .amount(event.getTotal())
                .build();
    }

    private boolean simulatePayment(BigDecimal amount){
        return Math.random() > 0.1; // 90% of payment are successful
    }
}
