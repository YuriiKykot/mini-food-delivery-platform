package com.fooddelivery.notificationservice.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;

public interface NotificationService {
    void sendNotification(PaymentCompletedEvent event);
}
