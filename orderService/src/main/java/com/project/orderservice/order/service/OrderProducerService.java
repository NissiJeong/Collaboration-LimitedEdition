package com.project.orderservice.order.service;

import com.project.common.dto.KafkaMessage;
import com.project.orderservice.order.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducerService {

    private final KafkaTemplate<String, KafkaMessage<?>> productKafkaTemplate;

    public void sendMessage(String topic, KafkaMessage<OrderRequestDto> message) {
        productKafkaTemplate.send(topic, message);
    }
}
