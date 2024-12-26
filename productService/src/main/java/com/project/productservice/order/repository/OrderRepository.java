package com.project.productservice.order.repository;

import com.project.productservice.order.entity.Order;
import com.project.productservice.order.entity.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Transactional
    @Modifying
    @Query("""
            UPDATE Order o
               SET o.orderStatus = 'IN_DELIVERY'
             WHERE o.orderStatus = 'ORDER_COMPLETE'
               AND o.createdAt <= :threshold
          """)
    int updateToShipping(LocalDateTime threshold);

    @Transactional
    @Modifying
    @Query("""
            UPDATE Order o
               SET o.orderStatus = 'DELIVERY_COMPLETE'
                 , o.deliveredAt =CURRENT_TIMESTAMP
             WHERE o.orderStatus = 'IN_DELIVERY'
               AND o.createdAt <= :threshold
          """)
    int updateToDelivered(LocalDateTime threshold);

    @Query("""
            SELECT o
              FROM Order o
             WHERE o.orderStatus = 'IN_REFUND_DELIVERY'
               AND o.modifiedAt < :threshold
          """)
    List<Order> findAllByYesterdayAndInRefund(LocalDateTime threshold);

    Optional<Order> findByIdAndOrderStatus(Long orderId, OrderStatusEnum orderStatusEnum);
}
