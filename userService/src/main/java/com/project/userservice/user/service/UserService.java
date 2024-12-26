package com.project.userservice.user.service;

import com.project.userservice.jwt.JwtUtil;
import com.project.userservice.user.dto.UserDto;
import com.project.userservice.user.entity.User;
import com.project.userservice.user.entity.UserRoleEnum;
import com.project.userservice.user.repository.RedisRepository;
import com.project.userservice.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EncryptService encryptService;
    private final JwtUtil jwtUtil;
    private final RedisRepository redisRepository;

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

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if("refresh_token".equals(cookie.getName())) {
                    String refreshToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);

                    // refresh token 검증과 동시에 username 반환
                    String username = jwtUtil.validateToken(refreshToken);
                    if(username == null) {
                        throw new NullPointerException("refresh token 으로부터 User 정보를 가져올 수 없습니다.");
                    }
                    if (username.contains("TokenError:")) {
                        log.error("Token Error");
                        // 토큰 만료에 대한 정보 전달 -> 이후 사용자 /user/token/refresh 요청가능
                        switch (username) {
                            case "TokenError: Expired JWT token" ->
                                    throw new IllegalArgumentException("TokenError: Expired JWT token");
                            case "TokenError: Invalid JWT signature" ->
                                    throw new IllegalArgumentException("TokenError: Invalid JWT signature");
                            case "TokenError: Invalid token" ->
                                    throw new IllegalArgumentException("TokenError: Invalid token");
                        }
                        return;
                    }
                    String redisRefreshToken = redisRepository.getData(username+"_refreshToken");

                    // refresh 토큰이 유효하다면
                    Map<String, String> userInfo = jwtUtil.validationRefreshToken(refreshToken, redisRefreshToken, response).orElseThrow(() ->
                            new IllegalArgumentException("refresh token 이 유효하지 않습니다. 다시 로그인을 해야합니다.")
                    );

                    // accessToken , refreshToken 재발급
                    username = userInfo.get("username");
                    UserRoleEnum role = UserRoleEnum.valueOf(userInfo.get("role"));
                    String newRefreshToken = jwtUtil.createAccessTokenAndRefreshToken(username, role, response).orElseThrow(()->
                            new IllegalArgumentException("refresh token 생성 오류")
                    );

                    // Redis 에 refreshToken 만료시간 설정(90일)
                    redisRepository.setDataExpire(username+"_refreshToken", newRefreshToken, 90L * 24 * 60 * 60 * 1000L);
                }
            }
        }
    }
}
