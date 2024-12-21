package org.myteam.server.global.security.handler;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myteam.server.auth.repository.RefreshJpaRepository;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static org.springframework.http.HttpMethod.POST;

public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
    private static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    final JwtProvider jwtProvider;
    final RefreshJpaRepository refreshJpaRepository;

    public LogoutSuccessHandler(JwtProvider jwtProvider, RefreshJpaRepository refreshJpaRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshJpaRepository = refreshJpaRepository;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logger.info("LogoutSuccessHandler onLogoutSuccess() 메서드를 실행하였습니다");

        String method = request.getMethod();
        if (!method.equals(POST.name())) {
            return;
        }

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_CATEGORY_REFRESH)) {
                refresh = cookie.getValue();
            }
        }

        //refresh null check
        if (refresh == null) {
            throw new PlayHiveException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //expired check
        try {
            jwtProvider.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new PlayHiveException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtProvider.getCategory(refresh);
        if (!category.equals(TOKEN_CATEGORY_REFRESH)) {
            throw new PlayHiveException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UUID publicId = jwtProvider.getPublicId(refresh);

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshJpaRepository.existsByRefreshAndPublicId(refresh, publicId);
        if (!isExist) {
            throw new PlayHiveException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //로그아웃 진행
        //Refresh 토큰 DB 에서 제거
        refreshJpaRepository.deleteByRefreshAndPublicId(refresh, publicId);

        // 1. Security Context 해제
        SecurityContextHolder.clearContext();

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
