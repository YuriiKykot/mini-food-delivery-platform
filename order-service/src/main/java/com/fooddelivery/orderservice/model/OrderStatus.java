package com.fooddelivery.orderservice.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PROCESSING,
    PAID,
    FAILED,
    CANCELLED
}