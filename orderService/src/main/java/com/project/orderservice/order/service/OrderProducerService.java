package com.project.orderservice.order.service;

import com.project.common.dto.KafkaMessage;
import com.project.orderservice.order.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderProducerService {

    private final KafkaTemplate<String, KafkaMessage<?>> productKafkaTemplate;

    public void sendMessage(String topic, KafkaMessage<OrderRequestDto> message, Long userId) {
        // ProducerRecord 에 헤더를 추가
        ProducerRecord<String, KafkaMessage<?>> producerRecord = new ProducerRecord<>(
                topic,
                message.getType(),
                message
        );

        // 헤더 추가
        producerRecord.headers().add("X-Claim-sub", String.valueOf(userId).getBytes());
        producerRecord.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

        CompletableFuture.runAsync(() -> productKafkaTemplate.send(producerRecord))
                .thenAccept(result -> System.out.println("Kafka 메시지 전송 성공: " + message))
                .exceptionally(ex -> {
                    System.err.println("Kafka 메시지 전송 실패: " + message);
                    ex.printStackTrace();
                    return null;
                });
    }
}
