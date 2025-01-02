package com.project.paymentservice.payment.dto;

import com.project.paymentservice.payment.entity.PaymentStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRequestDto {
    private Long paymentId;
    private Long userId;
    private Long orderId;
    private PaymentStatusEnum paymentStatus;

    @Builder
    public PaymentRequestDto(Long paymentId, Long userId, Long orderId, PaymentStatusEnum paymentStatus) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
    }
}
