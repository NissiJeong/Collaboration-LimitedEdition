package com.project.orderservice.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Orders")
@NoArgsConstructor
@Getter
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private Long userId;

    private Long addressId;

    @Enumerated(value = EnumType.STRING)
    private OrderStatusEnum orderStatus;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public Order(Long userId, Long addressId) {
        this.userId = userId;
        this.addressId = addressId;
        this.orderStatus = OrderStatusEnum.ORDER_COMPLETE;
    }

    public void updateStats(OrderStatusEnum orderStatus) {
        this.orderStatus = orderStatus;
    }

    public boolean canBeReturned() {
        if (!OrderStatusEnum.DELIVERY_COMPLETE.equals(this.orderStatus)) {
            return false; // 반품 불가 상태
        }
        if (deliveredAt == null) {
            return false; // 배송 완료 시간 누락
        }
        LocalDateTime now = LocalDateTime.now();
        return deliveredAt.plusDays(1).isAfter(now); // D+1 이내 확인
    }
}
