package com.project.orderservice.order.controller;

import com.project.common.dto.ResponseMessage;
import com.project.orderservice.order.dto.OrderRequestDto;
import com.project.orderservice.order.dto.OrderResponseDto;
import com.project.orderservice.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ResponseMessage> registerOrder(@RequestBody OrderRequestDto orderRequestDto, HttpServletRequest request) {
        OrderResponseDto orderResponseDto = orderService.saveOrder(orderRequestDto, request);

        ResponseMessage response = ResponseMessage.builder()
                .data(orderResponseDto)
                .statusCode(200)
                .resultMessage("주문 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ResponseMessage> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody OrderRequestDto orderRequestDto, HttpServletRequest request) {
        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(orderId, orderRequestDto, request);

        ResponseMessage response = ResponseMessage.builder()
                .data(orderResponseDto)
                .statusCode(200)
                .resultMessage("주문 수정 성공").build();

        return ResponseEntity.status(200).body(response);
    }
}
