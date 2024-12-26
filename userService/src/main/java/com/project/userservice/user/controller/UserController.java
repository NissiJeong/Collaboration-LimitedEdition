package com.project.userservice.user.controller;

import com.project.userservice.user.dto.EmailDto;
import com.project.userservice.user.dto.UserDto;
import com.project.userservice.user.service.EmailService;
import com.project.userservice.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDto requestDto) {
        UserDto user = userService.signUp(requestDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping(value = "/mail/certification/code")
    public ResponseEntity<?> requestEmailCode(@RequestBody EmailDto requestDto) throws MessagingException {
        emailService.sendEmail(requestDto.getEmail());
        return ResponseEntity.ok("인증코드가 발송되었습니다.");
    }

    @PostMapping(value = "/mail/verification/code")
    public ResponseEntity<?> verifyEmailCode(@RequestBody EmailDto requestDto) {
        boolean isVerify = emailService.verifyEmailCode(requestDto.getEmail(), requestDto.getVerifyCode());
        return ResponseEntity.ok(isVerify?"인증이 완료되었습니다.":"인증이 실패했습니다.");
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        userService.refreshAccessToken(request, response);
        return ResponseEntity.ok("accessToken 재발급 완료");
    }
}
