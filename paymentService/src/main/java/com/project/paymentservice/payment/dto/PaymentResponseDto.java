package com.project.paymentservice.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private Long orderId;
    private Long userId;

    @Builder
    public PaymentResponseDto(Long paymentId, Long orderId, Long userId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
    }
}
