package com.project.orderservice.feignclient.payment;

import com.project.orderservice.feignclient.dto.PaymentDto;
import com.project.orderservice.order.dto.OrderRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service")
public interface PaymentFeign {

    @PostMapping("/api/payment")
    PaymentDto registerPayment(@RequestHeader("X-Claim-sub") Long userId, @RequestBody OrderRequestDto orderRequestDto);
}
