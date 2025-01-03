package com.project.orderservice.order.service;

import com.project.orderservice.feignclient.payment.PaymentFeign;
import com.project.orderservice.feignclient.product.ProductFeign;
import com.project.orderservice.order.dto.OrderProductDto;
import com.project.orderservice.order.dto.OrderRequestDto;
import com.project.orderservice.order.dto.ProductDto;
import com.project.orderservice.order.repository.OrderProductRepository;
import com.project.orderservice.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private PaymentFeign paymentFeign;

    @Test
    @DisplayName("동시에 100개 상품 구매 요청")
    public void threadCreateOrder() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Claim-sub", "1");

        List<OrderProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(OrderProductDto.builder().productId(4L).orderQuantity(10).build());

        OrderRequestDto orderRequestDto = OrderRequestDto.builder()
                .addressId(1L)
                .orderProductDtoList(productDtoList).build();

        for(int i=0; i<threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    orderService.saveOrder(orderRequestDto, request);
                } catch (Exception e) {
                    System.err.println("Thread " + finalI + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ProductDto productDto = productFeign.getProduct(1L).orElseThrow();

        assertEquals(0, productDto.getStock());
    }
}