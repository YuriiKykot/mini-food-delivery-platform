package com.fooddelivery.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {

    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime processedAt;
}
