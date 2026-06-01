package com.fooddelivery.paymentsevice.kafka.consumer;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.paymentsevice.kafka.producer.PaymentEventProducer;
import com.fooddelivery.paymentsevice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "order-created",
            groupId = "payment-service-group"
    )
    public void consume(OrderCreatedEvent event){
        log.info("Received OrderCreatedEvent for orderId = {}", event.getOrderId());

        try {
            PaymentCompletedEvent paymentCompletedEvent = paymentService.processPayment(event);
            paymentEventProducer.sendPaymentCompletedEvent(paymentCompletedEvent);
        }catch (Exception e){
            log.error("Failed to process payment for orderId = {} : {}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
