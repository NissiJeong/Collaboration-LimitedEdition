package com.project.collaboration.order.repository;

import com.project.collaboration.order.entity.Order;
import com.project.collaboration.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrder(Order order);
}
