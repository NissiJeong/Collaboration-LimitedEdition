package com.project.productservice.product.service;

import com.project.common.dto.KafkaMessage;
import com.project.productservice.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductConsumerService {

    private final ProductService productService;

    @KafkaListener(topics = "order-topic") // groupId는 application.yml 에서 자동으로 읽힘
    public void consumerOrderEvent(ConsumerRecord<String, KafkaMessage<?>> record) {
        // 메시지 본문
        KafkaMessage<?> message = record.value();

        // 메시지 헤더 읽기
        Header timestampHeader = record.headers().lastHeader("timestamp");

        // 헤더 값이 존재하면 String으로 변환
        String timestamp = (timestampHeader != null) ? new String(timestampHeader.value()) : null;

        log.info("kafka message consume: {}",timestamp);

        String type = message.getType();
        if("order_create".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            List<Map<String, Object>> orderProductDtoList = (List<Map<String, Object>>) data.get("orderProductDtoList");

            for(Map<String, Object> product : orderProductDtoList) {
                Long productId = Long.parseLong(product.get("productId").toString());
                int orderQuantity = Integer.parseInt(product.get("orderQuantity").toString());

                productService.changeProductStockByOrder(productId, ProductDto.builder().orderQuantity(orderQuantity).build(), "minus");
            }
        }
    }
}
