package com.project.paymentservice.payment.service;

import com.project.common.dto.KafkaMessage;
import com.project.paymentservice.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConsumerService {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-topic")
    public void consumerOrderEvent(ConsumerRecord<String, KafkaMessage<PaymentRequestDto>> record) {
        // 메시지 본문
        KafkaMessage<PaymentRequestDto> message = record.value();

        // 메시지 헤더 읽기
        Header userIdHeader = record.headers().lastHeader("X-Claim-sub");
        Header timestampHeader = record.headers().lastHeader("timestamp");

        // 헤더 값이 존재하면 String으로 변환
        String userId = (userIdHeader != null) ? new String(userIdHeader.value()) : null;
        String timestamp = (timestampHeader != null) ? new String(timestampHeader.value()) : null;

        log.info("kafka message consume: {}",timestamp);

        String type = message.getType();
        if("order_create".equals(type)) {
            if(userId == null)
                throw new IllegalArgumentException("userId must not be null");

            Map<String, Object> data = (Map<String, Object>) message.getData();
            paymentService.savePayment(
                    PaymentRequestDto.builder().orderId(Long.parseLong(String.valueOf(data.get("orderId")))).build(),
                    Long.parseLong(userId));
        }
    }
}
