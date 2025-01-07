package com.project.orderservice.order.service;

import com.project.common.dto.KafkaMessage;
import com.project.orderservice.order.dto.OrderRequestDto;
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
public class OrderConsumerService {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-topic")
    public void consumerPaymentEvent(ConsumerRecord<String, KafkaMessage<OrderRequestDto>> record) {
        KafkaMessage<OrderRequestDto> message = record.value();

        // 메시지 헤더 읽기
        Header userIdHeader = record.headers().lastHeader("X-Claim-sub");
        Header timestampHeader = record.headers().lastHeader("timestamp");

        // 헤더 값이 존재하면 String 으로 변환
        String userId = (userIdHeader != null) ? new String(userIdHeader.value()) : null;
        String timestamp = (timestampHeader != null) ? new String(timestampHeader.value()) : null;

        log.info("kafka message consume: {}",timestamp);

        String type = message.getType();
        if("payment_complete".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            orderService.completeOrder(OrderRequestDto.builder()
                    .orderId(Long.parseLong(String.valueOf(data.get("orderId"))))
                    .paymentId(Long.parseLong(String.valueOf(data.get("paymentId"))))
                    .build());
        }
        else if("payment_cancel".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            orderService.cancelOrder(OrderRequestDto.builder()
                    .orderId(Long.parseLong(String.valueOf(data.get("orderId"))))
                    .paymentId(Long.parseLong(String.valueOf(data.get("paymentId"))))
                    .build());
        }
    }
}
