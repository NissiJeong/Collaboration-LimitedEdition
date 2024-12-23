package com.project.collaboration.order.controller;

import com.project.collaboration.order.dto.OrderRequestDto;
import com.project.collaboration.order.dto.OrderResponseDto;
import com.project.collaboration.order.service.OrderService;
import com.project.collaboration.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> registerOrder(@RequestBody OrderRequestDto orderRequestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto orderResponseDto = orderService.saveOrder(orderRequestDto, userDetails);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody OrderRequestDto orderRequestDto,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(orderId, orderRequestDto, userDetails);
        return ResponseEntity.ok(orderResponseDto);
    }
}
