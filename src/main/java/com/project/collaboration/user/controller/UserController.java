package com.project.collaboration.user.controller;

import com.project.collaboration.user.dto.EmailDto;
import com.project.collaboration.user.dto.UserDto;
import com.project.collaboration.user.service.EmailService;
import com.project.collaboration.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping(value = "/mail/virification/code")
    public ResponseEntity<?> virifyEmailCode(@RequestBody EmailDto requestDto) {
        boolean isVerify = emailService.verifyEmailCode(requestDto.getEmail(), requestDto.getVerifyCode());
        return ResponseEntity.ok(isVerify?"인증이 완료되었습니다.":"인증이 실패했습니다.");
    }
}
