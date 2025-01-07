package com.project.productservice.product.service;

import com.project.common.dto.KafkaMessage;
import com.project.productservice.product.dto.PaymentOrderDto;
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
        Map<String, Object> data = (Map<String, Object>) message.getData();
        if("order_create".equals(type)) {
            List<Map<String, Object>> orderProductDtoList = (List<Map<String, Object>>) data.get("orderProductDtoList");
            /*
            주문 생성 시에는 MySQL 재고 감소 X, 결제 확정 시에 재고 감소
            for(Map<String, Object> product : orderProductDtoList) {
                Long productId = Long.parseLong(product.get("productId").toString());
                int orderQuantity = Integer.parseInt(product.get("orderQuantity").toString());

                productService.changeProductStockByOrder(productId, ProductDto.builder().orderQuantity(orderQuantity).build(), "minus");
            }
             */
        }
        else if("order_cancel".equals(type)) {
            List<Map<String, Object>> orderProductDtoList = (List<Map<String, Object>>) data.get("orderProductDtoList");

            for(Map<String, Object> product : orderProductDtoList) {
                Long productId = Long.parseLong(product.get("productId").toString());
                int orderQuantity = Integer.parseInt(product.get("orderQuantity").toString());

                productService.changeProductStockByOrder(productId, ProductDto.builder().orderQuantity(orderQuantity).build(), "plus");
            }
        }
    }

    @KafkaListener(topics = "payment-topic")
    public void consumerPaymentEvent(ConsumerRecord<String, KafkaMessage<PaymentOrderDto>> record) {
        KafkaMessage<PaymentOrderDto> message = record.value();

        // 메시지 헤더 읽기
        Header userIdHeader = record.headers().lastHeader("X-Claim-sub");
        Header timestampHeader = record.headers().lastHeader("timestamp");

        // 헤더 값이 존재하면 String으로 변환
        String userId = (userIdHeader != null) ? new String(userIdHeader.value()) : null;
        String timestamp = (timestampHeader != null) ? new String(timestampHeader.value()) : null;

        log.info("kafka message consume: {}",timestamp);

        String type = message.getType();
        if("payment_complete".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            productService.changeProductStockByPayment(
                    PaymentOrderDto.builder()
                            .paymentId(Long.parseLong(String.valueOf(data.get("paymentId"))))
                            .orderId(Long.parseLong(String.valueOf(data.get("orderId")))).build(),
                    "minus"
            );
        }
        else if("payment_cancel".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            productService.restockProductStockByPaymentCancel(
                    PaymentOrderDto.builder()
                            .paymentId(Long.parseLong(String.valueOf(data.get("paymentId"))))
                            .orderId(Long.parseLong(String.valueOf(data.get("orderId")))).build(),
                    "plus"
            );
        }
    }
}
