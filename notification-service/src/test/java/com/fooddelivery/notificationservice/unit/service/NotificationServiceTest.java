package com.fooddelivery.notificationservice.unit.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import com.fooddelivery.notificationservice.service.NotificationService;
import com.fooddelivery.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("sendNotification should not throw when payment status is SUCCESS")
    void setNotificationService_should_notThrow_when_paymentSuccess(){
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("28.48"))
                .paymentStatus("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();

        assertThatCode(() -> notificationService.sendNotification(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendNotification should not throw when payment status is FAILED")
    void setNotificationService_should_notThrow_when_paymentFailed(){
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("28.48"))
                .paymentStatus("FAILED")
                .processedAt(LocalDateTime.now())
                .build();

        assertThatCode(() -> notificationService.sendNotification(event))
                .doesNotThrowAnyException();
    }
}
