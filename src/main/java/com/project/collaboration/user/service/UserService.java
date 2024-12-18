package com.project.collaboration.user.service;

import com.project.collaboration.user.dto.UserDto;
import com.project.collaboration.user.entity.User;
import com.project.collaboration.user.entity.UserRoleEnum;
import com.project.collaboration.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto signUp(UserDto requestDto) {
        String userName = requestDto.getUserName();
        String password = passwordEncoder.encode(requestDto.getPassword());
        String email = requestDto.getEmail();
        String mobileNumber = requestDto.getMobileNumber();
        String address = requestDto.getAddress();

        // email 중복확인
        Optional<User> checkUser = userRepository.findByEmail(requestDto.getEmail());
        if(checkUser.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        User user = new User().builder()
                .userName(userName)
                .password(password)
                .email(email)
                .mobileNumber(mobileNumber)
                .address(address)
                .role(UserRoleEnum.USER).build();

        User saveUser = userRepository.save(user);

        return new UserDto.Builder()
                .userName(saveUser.getUserName())
                .email(saveUser.getEmail())
                .mobileNumber(saveUser.getMobileNumber())
                .address(saveUser.getAddress()).build();
    }
}
