package com.project.paymentservice.payment.service;

import com.project.common.dto.KafkaMessage;
import com.project.paymentservice.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducerService {
    private final KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    public void sendMessage(String topic, KafkaMessage<PaymentRequestDto> message, Long userId) {
        // ProducerRecord 에 헤더를 추가
        ProducerRecord<String, KafkaMessage<?>> producerRecord = new ProducerRecord<>(
                topic,
                message.getType(),
                message
        );

        // 헤더 추가
        producerRecord.headers().add("X-Claim-sub", String.valueOf(userId).getBytes());
        producerRecord.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

        kafkaTemplate.send(producerRecord);
    }
}
