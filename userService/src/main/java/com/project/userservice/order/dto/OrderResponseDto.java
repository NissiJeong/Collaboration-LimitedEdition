package com.project.userservice.order.dto;

import com.project.userservice.order.entity.OrderStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class OrderResponseDto {
    private Long orderId;
    private OrderStatusEnum orderStatus;

    @Builder
    public OrderResponseDto(Long orderId, OrderStatusEnum orderStatus) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }
}
