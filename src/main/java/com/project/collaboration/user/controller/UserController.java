package com.project.collaboration.user.controller;

import com.project.collaboration.user.dto.UserDto;
import com.project.collaboration.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDto requestDto) {
        UserDto user = userService.signUp(requestDto);
        return ResponseEntity.ok(user);
    }
}
