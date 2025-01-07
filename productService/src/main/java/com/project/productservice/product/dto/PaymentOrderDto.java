package com.project.productservice.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentOrderDto {
    private Long orderId;
    private Long paymentId;

    @Builder
    public PaymentOrderDto(Long orderId, Long paymentId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
    }
}
