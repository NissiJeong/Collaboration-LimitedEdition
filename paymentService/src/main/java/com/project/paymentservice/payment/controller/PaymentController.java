package com.project.paymentservice.payment.controller;

import com.project.common.dto.ResponseMessage;
import com.project.paymentservice.payment.dto.PaymentRequestDto;
import com.project.paymentservice.payment.dto.PaymentResponseDto;
import com.project.paymentservice.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결재 진입 api
     * @param requestDto
     * @return
     */
    @PostMapping
    public ResponseEntity<?> registerPayment(@RequestBody PaymentRequestDto requestDto, HttpServletRequest request) {
        PaymentResponseDto responseDto = paymentService.savePayment(requestDto, request);

        ResponseMessage responseMessage = ResponseMessage.builder()
                .statusCode(200)
                .data(responseDto)
                .resultMessage("결제 진입 성공").build();

        return ResponseEntity.status(200).body(responseMessage);
    }
}
