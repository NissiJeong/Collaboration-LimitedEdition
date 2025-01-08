package com.project.paymentservice.payment.service;

import com.project.common.dto.KafkaMessage;
import com.project.common.repository.RedisRepository;
import com.project.paymentservice.payment.dto.PaymentRequestDto;
import com.project.paymentservice.payment.dto.PaymentResponseDto;
import com.project.paymentservice.payment.entity.Payment;
import com.project.paymentservice.payment.entity.PaymentStatusEnum;
import com.project.paymentservice.payment.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducerService producerService;
    private final RedisRepository redisRepository;

    public PaymentResponseDto savePayment(PaymentRequestDto requestDto, Long userId) {
        Payment payment = paymentRepository.save(new Payment(requestDto, userId, PaymentStatusEnum.IN_PROGRESS));

        return PaymentResponseDto.builder()
                .userId(userId)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId()).build();
    }

    @Transactional
    public PaymentResponseDto completePayment(Long paymentId, HttpServletRequest request) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()->
                new NullPointerException("해당 결제 정보가 없습니다.")
        );

        // 20% 확률로 결제 취소
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            // 결제 취소 이벤트 발생: order service 주문 취소, product service 재고 복구
            producerService.sendMessage(
                    "payment-topic",
                    new KafkaMessage<>(
                            "payment_cancel",
                            PaymentRequestDto.builder()
                                    .orderId(payment.getOrderId())
                                    .paymentId(payment.getId()).build()
                    ),
                    null
            );
            return PaymentResponseDto.builder().paymentId(paymentId).status(PaymentStatusEnum.FAIL).build();
        }

        // Redis 에 있는 예약 내역 만료 시간 연장(정상 결제 위해서)
        String key = "reservation:order:"+payment.getOrderId();
        redisRepository.extendExpire(key, 10);

        // 결제 상태값 업데이트: 결제 완료
        payment.changeStatus(PaymentStatusEnum.COMPLETE);

        // 결제 완료 이벤트 발생: order service 주문 완료, Product service 재고 감소
        producerService.sendMessage(
                "payment-topic",
                new KafkaMessage<>(
                        "payment_complete",
                        PaymentRequestDto.builder()
                                .paymentId(payment.getId())
                                .orderId(payment.getOrderId()).build()),
                null);

        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus()).build();
    }

    public Long getPaymentIdByOrderId(Long orderId) {
        Payment payment = paymentRepository.getPaymentByOrderId(orderId);

        return payment.getId();
    }
}
