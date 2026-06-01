package com.fooddelivery.orderservice.kafka.comsumer;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "order-service-group"
    )
    public void consume(PaymentCompletedEvent event){
        log.info("Received PaymentCompletedEvent for orderOd = {}, status = {}",
                event.getOrderId(), event.getPaymentStatus());

        orderService.updateOrderStatus(event);
    }
}
