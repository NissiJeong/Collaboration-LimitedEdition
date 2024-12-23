package com.project.collaboration.order.repository;

import com.project.collaboration.order.entity.Order;
import com.project.collaboration.order.entity.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = 'SHIPPING' WHERE o.orderStatus = 'ORDER_COMPLETE' AND o.createdAt <= :threshold")
    int updateToShipping(LocalDateTime threshold);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = 'DELIVERED' WHERE o.orderStatus = 'IN_DELIVERY' AND o.createdAt <= :threshold")
    int updateToDelivered(LocalDateTime threshold);

    Optional<Order> findByIdAndOrderStatus(Long orderId, OrderStatusEnum orderStatusEnum);
}
