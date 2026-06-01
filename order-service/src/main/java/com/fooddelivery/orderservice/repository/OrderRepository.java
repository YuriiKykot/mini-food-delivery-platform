package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByCustomerId(Long customerId);
}
