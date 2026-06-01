package com.fooddelivery.orderservice.exception;

public class ItemNotFoundException extends RuntimeException{
    public ItemNotFoundException(Long id) {
        super("item not found: " + id);
    }
}
