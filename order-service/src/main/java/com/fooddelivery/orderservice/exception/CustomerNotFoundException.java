package com.fooddelivery.orderservice.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Customer not found: " + id);
    }
}
