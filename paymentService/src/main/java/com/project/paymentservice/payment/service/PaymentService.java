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

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponseDto savePayment(PaymentRequestDto requestDto, Long userId) {
        Payment payment = paymentRepository.save(new Payment(requestDto, userId, PaymentStatusEnum.IN_PROGRESS));

        return PaymentResponseDto.builder()
                .userId(userId)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId()).build();
    }

    public PaymentResponseDto completePayment(Long paymentId, HttpServletRequest request) {
        // 20% 확률로 결제 취소
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            // TODO 해당 주문에 대한 상품 재고 복구 로직 추가
            return PaymentResponseDto.builder().paymentId(paymentId).status(PaymentStatusEnum.FAIL).build();
        }

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()->
                new NullPointerException("해당 결제 정보가 없습니다.")
        );

        payment.changeStatus(PaymentStatusEnum.COMPLETE);

        // TODO 해당 결제와 연결된 Order 결제 완료 요청


        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus()).build();
    }
}
