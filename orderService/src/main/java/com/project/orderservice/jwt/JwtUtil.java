package com.project.orderservice.jwt;

import com.project.orderservice.user.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j(topic = "JwtUtil")
@Component
@RequiredArgsConstructor
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";


    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createToken(String username, UserRoleEnum role, String type) {
        // 토큰 만료시간
        long TOKEN_TIME = 0; // 60분
        if(type.equals("accessToken")) {
            TOKEN_TIME = 60 * 60 * 1000L;
        }
        else {
            TOKEN_TIME = 90L * 24 * 60 * 60 * 1000L; // 3개월 (90일)
        }
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // header 에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getTokenWithoutBearer(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    // 토큰 검증
    public String validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            // 토큰이 유효하다면 사용자 식별자(subject) 반환
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
            return "TokenError: Invalid JWT signature";
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
            return "TokenError: Expired JWT token";
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
            return "TokenError: Invalid token";
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            return "TokenError: Invalid token";
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        token = getTokenWithoutBearer(token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Optional<Map<String, String>> validationRefreshToken(String cookieRefreshToken, String redisRefreshToken, HttpServletResponse res) throws IOException  {
        if (StringUtils.hasText(cookieRefreshToken)) {
            cookieRefreshToken = getTokenWithoutBearer(cookieRefreshToken);
            redisRefreshToken = getTokenWithoutBearer(redisRefreshToken);

            String validToken = validateToken(cookieRefreshToken);
            if (validToken.contains("TokenError:")) {
                log.error("Token Error");

                // 토큰 만료에 대한 정보 전달 -> 이후 사용자 /user/token/refresh 요청가능
                if(validToken.equals("TokenError: Expired JWT token")) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
                    res.getWriter().write("Your access token has expired. Please use refresh token to obtain a new access token.");
                }

                return Optional.empty();
            }

            Claims info = getUserInfoFromToken(cookieRefreshToken);
            String username = info.getSubject();
            String role = info.get("auth").toString();

            if(cookieRefreshToken.equals(redisRefreshToken)) {
                return Optional.of(Map.of("username",username, "role", role));
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Optional<String> createAccessTokenAndRefreshToken(String username, UserRoleEnum role, HttpServletResponse response) {
        String token = createToken(username, role, "accessToken");
        String newRefreshToken = createToken(username, role, "refreshToken");

        // accessToken 바인딩
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);

        return newRefreshToken.describeConstable();
    }
}