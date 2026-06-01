package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item,Long> {

}
