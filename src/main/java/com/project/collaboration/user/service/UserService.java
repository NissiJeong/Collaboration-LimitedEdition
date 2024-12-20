package com.project.collaboration.user.service;

import com.project.collaboration.user.dto.UserDto;
import com.project.collaboration.user.entity.User;
import com.project.collaboration.user.entity.UserRoleEnum;
import com.project.collaboration.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EncryptService encryptService;

    public UserDto signUp(UserDto requestDto) {
        String userName = encryptService.encryptInfo(requestDto.getUserName());
        String password = passwordEncoder.encode(requestDto.getPassword());
        String email = encryptService.encryptInfo(requestDto.getEmail());
        String mobileNumber = encryptService.encryptInfo(requestDto.getMobileNumber());
        String address = encryptService.encryptInfo(requestDto.getAddress());
        String verifyCode = requestDto.getVerifyCode();

        if(verifyCode==null || verifyCode.isBlank() || verifyCode.isEmpty()) {
            throw new IllegalArgumentException("인증 코드를 입력해야 합니다.");
        }

        // 인증 실패할 경우
        if(!emailService.verifyEmailCode(requestDto.getEmail(), verifyCode)) {
            throw new IllegalArgumentException("이메일 인증에 실패했습니다.");
        }

        // email 중복확인
        Optional<User> checkUser = userRepository.findByEmail(email);
        if(checkUser.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        User user = new User(userName, password, email, mobileNumber, address, UserRoleEnum.USER);

        User saveUser = userRepository.save(user);

        return UserDto.builder()
                .userName(saveUser.getUserName())
                .email(saveUser.getEmail())
                .mobileNumber(saveUser.getMobileNumber())
                .address(saveUser.getAddress()).build();
    }
}
