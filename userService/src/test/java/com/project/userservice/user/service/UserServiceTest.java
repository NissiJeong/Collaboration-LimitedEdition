package com.project.userservice.user.service;

import com.project.common.exception.BizRuntimeException;
import com.project.common.repository.RedisRepository;
import com.project.userservice.jwt.JwtUtil;
import com.project.userservice.user.dto.UserDto;
import com.project.userservice.user.entity.User;
import com.project.userservice.user.entity.UserRoleEnum;
import com.project.userservice.user.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
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
                .email("jnissi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);
        when(redisRepository.getData("jnissi92@gmail.com:verify")).thenReturn("Y");
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        UserDto result = userService.signUp(userDto);

        assertThat(result.getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("유저 회원가입 테스트: 이메일 인증 실패한 경우")
    void signUpTest3() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnissi92@gmail.com")
                .verifyCode("1111").build();

        when(redisRepository.getData("jnissi92@gmail.com:verify")).thenReturn("");
        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);

        Exception exception = assertThrows(BizRuntimeException.class, () -> {
            userService.signUp(userDto);
        });

        assertEquals("이메일 인증이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("유저 회원가입 테스트: 중복된 이메일")
    void signUpTest4() {
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnissi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);
        when(redisRepository.getData("jnissi92@gmail.com:verify")).thenReturn("Y");
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.of(user));
        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);

        Exception exception = assertThrows(BizRuntimeException.class, () -> {
            userService.signUp(userDto);
        });

        assertEquals("중복된 Email 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트")
    void refreshTokenTest1() throws IOException {
        // Given
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnissi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);

        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        Long userId = 1L;
        String username = "testUser";
        String redisToken = "mockRedisToken";
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", "USER");

        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("username");
        when(redisRepository.getData(anyString())).thenReturn(redisToken);
        when(jwtUtil.validationRefreshToken("mockRefreshToken", redisToken, response)).thenReturn(Optional.of(userInfo));
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessTokenAndRefreshToken(username, null,UserRoleEnum.USER, response)).thenReturn(Optional.of("newMockRefreshToken"));

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        userService.refreshAccessToken(request, response);
    }

    /**
     * 해당 테스트 진행 중 쿠키값에 저장되어 있는 Refresh token 값이 유효한지 체크 하는 로직이 없다는 것을 확인.
     */
    @Test
    @DisplayName("refresh token 테스트: 토큰으로부터 가져온 사용자 정보가 유효하지 않은 경우")
    void refreshTokenTest2() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
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

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("refresh token 이 유효하지 않습니다. 다시 로그인을 해야합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: Refresh token 생성 오류")
    void refreshTokenTest4() throws IOException {
        // Given
        UserDto userDto = UserDto.builder()
                .userName("홍길동")
                .password("12345678")
                .email("jnissi92@gmail.com")
                .verifyCode("1111").build();

        User user = new User(userDto.getUserName(), userDto.getPassword(), userDto.getEmail(), userDto.getMobileNumber(), userDto.getAddress(), null);

        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};
        when(request.getCookies()).thenReturn(cookies);

        Long userId = 1L;
        String username = "testUser";
        String redisToken = "mockRedisToken";
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", "USER");

        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("username");
        when(redisRepository.getData(anyString())).thenReturn(redisToken);
        when(jwtUtil.validationRefreshToken("mockRefreshToken", redisToken, response)).thenReturn(Optional.of(userInfo));
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessTokenAndRefreshToken(eq(username), eq(null), eq(UserRoleEnum.USER), eq(response))).thenReturn(Optional.empty());

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("refresh token 생성 오류", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: 유효한 토큰이 아닐 때")
    void refreshTokenTest5() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("TokenError: Expired JWT token");

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("TokenError: Expired JWT token", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: 유효한 토큰이 아닐 때")
    void refreshTokenTest6() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("TokenError: Invalid JWT signature");

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("TokenError: Invalid JWT signature", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: 유효한 토큰이 아닐 때")
    void refreshTokenTest7() throws IOException {
        // Given
        Cookie[] cookies = {new Cookie("refresh_token", URLEncoder.encode("mockRefreshToken", StandardCharsets.UTF_8))};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.validateToken("mockRefreshToken")).thenReturn("TokenError: Invalid token");

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("TokenError: Invalid token", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: 사용자가 없을 때")
    void refreshTokenTest8() throws IOException {
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
        when(userRepository.findByEmail(nullable(String.class))).thenReturn(Optional.empty());

        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("사용자 정보가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("refresh token 테스트: 쿠키가 빈 값일 때")
    void refreshTokenTest9() {
        // Given
        UserService userService = new UserService(userRepository, passwordEncoder, encryptService, jwtUtil, redisRepository);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            userService.refreshAccessToken(request, response);
        });

        assertEquals("쿠키가 존재하지 않습니다.", exception.getMessage());
    }
}