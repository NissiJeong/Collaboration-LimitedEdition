package com.project.paymentservice.payment.dto;

import com.project.paymentservice.payment.entity.PaymentStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private PaymentStatusEnum status;

    @Builder
    public PaymentResponseDto(Long paymentId, Long orderId, Long userId, PaymentStatusEnum status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }
}
