package com.project.collaboration.user.service;

import com.project.collaboration.jwt.JwtUtil;
import com.project.collaboration.user.dto.UserDto;
import com.project.collaboration.user.entity.User;
import com.project.collaboration.user.entity.UserRoleEnum;
import com.project.collaboration.user.repository.RedisRepository;
import com.project.collaboration.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private EncryptService encryptService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisRepository redisRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("유저 회원가입 테스트")
    void signUpTest1() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnssi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);
        when(emailService.verifyEmailCode("jnssi92@gmail.com", "1111")).thenReturn(true);
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);
        UserDto result = userService.signUp(userDto);

        assertThat(result.getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("유저 회원가입 테스트: 인증 코드 없는 경우")
    void signUpTest2() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnssi92@gmail.com").build();

        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(userDto);
        });

        assertEquals("인증 코드를 입력해야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("유저 회원가입 테스트: 이메일 인증 실패한 경우")
    void signUpTest3() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnssi92@gmail.com")
                .verifyCode("1111").build();

        when(emailService.verifyEmailCode("jnssi92@gmail.com", "1111")).thenReturn(false);
        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(userDto);
        });

        assertEquals("이메일 인증에 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("유저 회원가입 테스트: 중복된 이메일")
    void signUpTest4() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnssi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);
        when(emailService.verifyEmailCode("jnssi92@gmail.com", "1111")).thenReturn(true);
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.of(user));
        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(userDto);
        });

        assertEquals("중복된 Email 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트")
    void refreshTokenTest1() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        String username = "testUser";
        String redisToken = "mockRedisToken";
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", "USER");

        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("username");
        when(redisRepository.getData(anyString())).thenReturn(redisToken);
        when(jwtUtil.validationRefreshToken("mockRefreshToken", redisToken, response)).thenReturn(Optional.of(userInfo));
        when(jwtUtil.createAccessTokenAndRefreshToken(eq(username), eq(UserRoleEnum.USER), eq(response))).thenReturn(Optional.of("newMockRefreshToken"));

        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);
        userService.refreshAccessToken(request, response);
    }

    /**
     * 해당 테스트 진행 중 쿠키값에 저장되어 있는 Refresh token 값이 유효한지 체크 하는 로직이 없다는 것을 확인.
     * TODO refresh token 유효한지 확인 로직 추가되어야 함.
     * -> refresh token validation code && username NullPointerException check
     */
    @Test
    @DisplayName("refresh token 테스트: 토큰으로부터 가져온 사용자 정보가 유효하지 않은 경우")
    void refreshTokenTest2() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("refresh token 으로부터 User 정보를 가져올 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: redis 로부터 토큰을 제대로 가져오지 못할 때")
    void refreshTokenTest3() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("username");
        when(redisRepository.getData(anyString())).thenReturn(null);

        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("refresh token 이 유효하지 않습니다. 다시 로그인을 해야합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: Refresh token 생성 오류")
    void refreshTokenTest4() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        String username = "testUser";
        String redisToken = "mockRedisToken";
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", "USER");

        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("username");
        when(redisRepository.getData(anyString())).thenReturn(redisToken);
        when(jwtUtil.validationRefreshToken("mockRefreshToken", redisToken, response)).thenReturn(Optional.of(userInfo));
        when(jwtUtil.createAccessTokenAndRefreshToken(eq(username), eq(UserRoleEnum.USER), eq(response))).thenReturn(Optional.empty());

        UserService userService = new UserService(userRepository, passwordEncoder, emailService, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("refresh token 생성 오류", exception.getMessage());
    }
}