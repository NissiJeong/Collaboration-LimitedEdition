package com.project.paymentservice.payment.entity;

import com.project.paymentservice.payment.dto.PaymentRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private Long orderId;

    private Long userId;

    @Enumerated(value = EnumType.STRING)
    private PaymentStatusEnum status;

    public Payment(PaymentRequestDto requestDto, Long userId, PaymentStatusEnum status) {
        this.orderId = requestDto.getOrderId();
        this.userId =  userId;
        this.status = status;
    }

    public void changeStatus(PaymentStatusEnum status) {
        this.status = status;
    }
}
