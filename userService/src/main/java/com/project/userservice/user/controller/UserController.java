package com.project.userservice.user.controller;

import com.project.common.dto.ResponseMessage;
import com.project.userservice.user.dto.EmailDto;
import com.project.userservice.user.dto.UserDto;
import com.project.userservice.user.service.EmailService;
import com.project.userservice.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ResponseMessage> signUp(@Valid @RequestBody UserDto requestDto) {
        UserDto user = userService.signUp(requestDto);

        ResponseMessage response = ResponseMessage.builder()
                .data(user)
                .statusCode(201)
                .resultMessage("회원가입 성공").build();

        return ResponseEntity.status(201).body(response);
    }

    @PostMapping(value = "/mail/certification/code")
    public ResponseEntity<ResponseMessage> requestEmailCode(@RequestBody EmailDto requestDto) throws MessagingException {
        log.info("email request dto: {}",requestDto.getEmail());
        emailService.sendEmail(requestDto.getEmail());

        ResponseMessage response = ResponseMessage.builder()
                .statusCode(200)
                .resultMessage("이메일 인증코드 발송 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @PostMapping(value = "/mail/verification/code")
    public ResponseEntity<ResponseMessage> verifyEmailCode(@RequestBody EmailDto requestDto) {
        boolean isVerify = emailService.verifyEmailCode(requestDto.getEmail(), requestDto.getVerifyCode());

        ResponseMessage response = ResponseMessage.builder()
                .statusCode(200)
                .resultMessage(isVerify?"이메일 인증 완료":"이메일 인증이 실패").build();

        return ResponseEntity.status(200).body(response);
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<ResponseMessage> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        userService.refreshAccessToken(request, response);

        ResponseMessage responseMessage = ResponseMessage.builder()
                .statusCode(200)
                .resultMessage("accessToken 재발급 완료").build();

        return ResponseEntity.status(200).body(responseMessage);
    }
}
