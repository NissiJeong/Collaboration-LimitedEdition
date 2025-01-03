package com.project.paymentservice.payment.service;

import com.project.paymentservice.payment.dto.PaymentRequestDto;
import com.project.paymentservice.payment.dto.PaymentResponseDto;
import com.project.paymentservice.payment.entity.Payment;
import com.project.paymentservice.payment.entity.PaymentStatusEnum;
import com.project.paymentservice.payment.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponseDto savePayment(PaymentRequestDto requestDto, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));
        Payment payment = paymentRepository.save(new Payment(requestDto, userId, PaymentStatusEnum.IN_PROGRESS));

        return PaymentResponseDto.builder()
                .userId(userId)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId()).build();
    }
}
