package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    List<Customer> findByEmail(String email);
}
