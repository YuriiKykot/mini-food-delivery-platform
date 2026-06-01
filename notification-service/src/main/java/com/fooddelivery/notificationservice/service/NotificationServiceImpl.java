package com.fooddelivery.notificationservice.service;

import com.fooddelivery.common.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService{

    @Override
    public void sendNotification(PaymentCompletedEvent event) {
        if("SUCCESS".equals(event.getPaymentStatus())){
            log.info("Email sent to customerId = {} - order {} paid successfully, amount = {}",
                    event.getCustomerId(), event.getOrderId(), event.getAmount());
        }else{
            log.warn("Email sent to customerId = {} - payment FAILED for order {}",
                    event.getCustomerId(), event.getOrderId());
        }
    }
}
