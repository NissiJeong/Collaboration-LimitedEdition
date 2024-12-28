package com.project.orderservice.order.controller;

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
    public ResponseEntity<?> registerOrder(@RequestBody OrderRequestDto orderRequestDto, HttpServletRequest request) {
        OrderResponseDto orderResponseDto = orderService.saveOrder(orderRequestDto, request);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody OrderRequestDto orderRequestDto, HttpServletRequest request) {
        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(orderId, orderRequestDto, request);
        return ResponseEntity.ok(orderResponseDto);
    }
}