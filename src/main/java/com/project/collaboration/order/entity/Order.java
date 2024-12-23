package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.order.dto.AddressDto;
import com.project.collaboration.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private OrderStatusEnum orderStatus;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public Order(User user, Address address) {
        this.user = user;
        this.address = address;
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
