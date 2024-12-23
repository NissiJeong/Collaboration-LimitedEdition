package com.project.collaboration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.collaboration.jwt.JwtUtil;
import com.project.collaboration.user.dto.LoginDto;
import com.project.collaboration.user.entity.UserRoleEnum;
import com.project.collaboration.user.repository.RedisRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final AesBytesEncryptor encryptor;
    private final RedisRepository redisRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AesBytesEncryptor encryptor, RedisRepository redisRepository) {
        this.jwtUtil = jwtUtil;
        this.encryptor = encryptor;
        this.redisRepository = redisRepository;
        setFilterProcessesUrl("/api/user/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
            // 요청으로 넘어온 이메일 암호화, 기 암호화된 이메일과 비교하기 위하여
            String encryptEmail = encryptInfo(requestDto.getEmail());
            requestDto.setEmail(encryptEmail);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            encryptEmail,
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        String token = jwtUtil.createToken(username, role, "accessToken");
        String refreshToken = jwtUtil.createToken(username, role, "refreshToken");
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);

        // refresh token 은 cookie 에 설정.
        // response.addHeader(JwtUtil.REFRESH_TOKEN, refreshToken);
        setRefreshTokenInCookie(refreshToken, response);
        // Redis 에 refreshToken 만료시간 설정(90일)
        redisRepository.setDataExpire(decryptInfo(username)+"_refreshToken", refreshToken, 90L * 24 * 60 * 60 * 1000L);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }

    public void setRefreshTokenInCookie(String refreshToken, HttpServletResponse response) {
        // Refresh Token을 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // JavaScript에서 접근 불가
        // refreshTokenCookie.setSecure(true);   // HTTPS에서만 전송
        refreshTokenCookie.setPath("/");      // 유효한 경로 설정
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30일 동안 유지
        response.addCookie(refreshTokenCookie);
    }

    public String encryptInfo(String info) {
        byte[] encrypt = encryptor.encrypt(info.getBytes(StandardCharsets.UTF_8));
        return byteArrayToString(encrypt);
    }

    public String decryptInfo(String encryptString) {
        byte[] decryptBytes = stringToByteArray(encryptString);
        byte[] decrypt = encryptor.decrypt(decryptBytes);
        return new String(decrypt, StandardCharsets.UTF_8);
    }

    public String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte abyte :bytes){
            sb.append(abyte);
            sb.append(" ");
        }
        return sb.toString();
    }

    public byte[] stringToByteArray(String byteString) {
        String[] split = byteString.split("\\s");
        ByteBuffer buffer = ByteBuffer.allocate(split.length);
        for (String s : split) {
            buffer.put((byte) Integer.parseInt(s));
        }
        return buffer.array();
    }
}