package com.fooddelivery.paymentsevice.service;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.event.PaymentCompletedEvent;

public interface PaymentService {
    PaymentCompletedEvent processPayment(OrderCreatedEvent event);
}
