package com.project.orderservice.order.dto;

import com.project.orderservice.order.entity.OrderStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class OrderResponseDto {
    private Long orderId;
    private OrderStatusEnum orderStatus;
    private Long productId;
    private int orderQuantity;

    @Builder
    public OrderResponseDto(Long orderId, OrderStatusEnum orderStatus, Long productId, int orderQuantity) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
    }
}
