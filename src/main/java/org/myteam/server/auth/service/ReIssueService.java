package org.myteam.server.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.auth.entity.Refresh;
import org.myteam.server.auth.repository.RefreshJpaRepository;
import org.myteam.server.global.exception.PlayHiveException;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.myteam.server.global.exception.ErrorCode.*;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static org.myteam.server.util.CookieUtil.getCookie;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReIssueService {
    private final JwtProvider jwtProvider;
    private final RefreshJpaRepository refreshJpaRepository;

    /**
     * Refresh Token 검증
     */
    public void validateRefreshToken(String refresh, UUID publicId) {
        log.info("refresh : {}, publicId: {}", refresh, publicId);

        // 리프레시 토큰 만료 여부 체크
        try {
            jwtProvider.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new PlayHiveException(REFRESH_TOKEN_EXPIRED);
        }

        // 리프레시 토큰 카테고리 검증
        String category = jwtProvider.getCategory(refresh);
        if (!category.equals(TOKEN_CATEGORY_REFRESH)) {
            throw new PlayHiveException(INVALID_ACCESS_TOKEN);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshJpaRepository.existsByRefreshAndPublicId(refresh, publicId);
        if (!isExist) {
            throw new PlayHiveException(INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * Refresh Token 정보 추출
     *
     * @param request
     * @return
     */
    public String extractRefreshToken(HttpServletRequest request) {
        // 쿠키로 부터 리프레시 토큰 추출
        Optional<Cookie> refreshTokenOP = getCookie(request, TOKEN_CATEGORY_REFRESH);
        if (refreshTokenOP.isEmpty()) {
            throw new PlayHiveException(INVALID_REFRESH_TOKEN);
        }
        return refreshTokenOP.map(Cookie::getValue).orElse(null);
    }

//    public Tokens reissueTokens(String refresh) {
//        log.info("Refresh token: {}", refresh);
//
//        UUID publicId = jwtProvider.getPublicId(refresh);
//        String role = jwtProvider.getRole(refresh);
//
//        // Generate new tokens
//        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofMinutes(10), publicId, role);
//        String newRefreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofHours(24), publicId, role);
//
//        return new Tokens(accessToken, newRefreshToken);
//    }

    public void addRefreshEntity(UUID publicId, String refresh, Duration duration) {
        log.info("addRefreshEntity > publicId: {}, refresh: {}, expiredMs: {}", publicId, refresh, duration.toMillis());
        Date date = new Date(System.currentTimeMillis() + duration.toMillis());

        Refresh refreshEntity = Refresh.builder()
                .publicId(publicId)
                .refresh(refresh)
                .expiration(date.toString())
                .build();
        refreshJpaRepository.save(refreshEntity);
    }

    public void deleteByRefreshAndPublicId(String refresh, UUID publicId) {
        refreshJpaRepository.deleteByRefreshAndPublicId(refresh, publicId);
    }
}
