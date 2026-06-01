package com.fooddelivery.notificationservice.kafka.consumer;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "notification-service-group"
    )
    public void consume(PaymentCompletedEvent event){
        log.info("Received PaymentCompletedEvent for orderId = {}", event.getOrderId());
        notificationService.sendNotification(event);
    }
}
