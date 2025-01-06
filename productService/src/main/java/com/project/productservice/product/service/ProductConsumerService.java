package com.project.productservice.product.service;

import com.project.common.dto.KafkaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductConsumerService {

    @KafkaListener(topics = "order-topic") // groupId는 application.yml 에서 자동으로 읽힘
    public void consumerOrderEvent(KafkaMessage<?> message) {
        switch (message.getType()) {
            case "order":
                System.out.println("order: "+message.getData());
                break;

        }
    }
}
