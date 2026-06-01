package com.fooddelivery.paymentsevice.kafka.producer;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC = "payment-completed";

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    public void sendPaymentCompletedEvent(PaymentCompletedEvent event){
        kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                   if(ex != null){
                        log.error("Failed to send PaymentCompletedEvent for orderId = {}: {}",
                                event.getOrderId(), event.getPaymentStatus());
                   }else{
                       log.info("PaymentCompletedEvent sent for orderId = {}, status = {}",
                               event.getOrderId(), event.getPaymentStatus());
                   }
                });
    }
}
