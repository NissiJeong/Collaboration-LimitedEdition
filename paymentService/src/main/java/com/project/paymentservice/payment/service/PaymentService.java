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
        //TODO Order 서비스에서 해당 주문에 대한 상품 목록들 조회
        //TODO Redis 에 주문하려는 상품들의 재고가 충분한지 조회

        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));
        Payment payment = paymentRepository.save(new Payment(requestDto, userId, PaymentStatusEnum.IN_PROGRESS));

        //TODO 결제 진입 성공 시 주문에 해당하는 상품들 재고 감소 처리(kafka 이용하여 product 서비스에 이벤트 전달, product 서비스는 이벤트 수신 후 redis, mysql 재고 데이터 업데이트)

        return PaymentResponseDto.builder()
                .userId(userId)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId()).build();
    }
}
