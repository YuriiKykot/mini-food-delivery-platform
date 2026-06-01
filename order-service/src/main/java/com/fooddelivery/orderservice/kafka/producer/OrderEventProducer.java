package com.fooddelivery.orderservice.kafka.producer;

import com.fooddelivery.common.event.OrderCreatedEvent;
import com.fooddelivery.orderservice.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendOrderCreatedEvent(Order order){
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();

        kafkaTemplate.send(TOPIC, String.valueOf(order.getId()),event)
                .whenComplete((result, ex) -> {
                    if(ex != null){
                        log.error("Failed to send OrderCreatedEvent for orderId = {}: {}",
                                order.getId(),ex.getMessage());
                    }else{
                        log.info("OrderCreatedEvent sent for orderId = {}, partition = {}, offset = {}",
                                order.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
