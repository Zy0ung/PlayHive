package org.myteam.server.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.auth.entity.Refresh;
import org.myteam.server.auth.repository.RefreshJpaRepository;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.myteam.server.auth.controller.ReIssueController.LOGOUT_PATH;
import static org.myteam.server.auth.controller.ReIssueController.TOKEN_REISSUE_PATH;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_ACCESS;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static org.myteam.server.util.cookie.CookieUtil.createCookie;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final String ACCESS_TOKEN_KEY = "Authorization";
    private static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshJpaRepository refreshJpaRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider, RefreshJpaRepository refreshJpaRepository) {
        setFilterProcessesUrl("/login");
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.refreshJpaRepository = refreshJpaRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 요청 본문에서 username과 password 추출
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

            String username = credentials.get("username");
            String password = credentials.get("password");

            log.info("로그인 요청 - username: {}, password: {}", username, password);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            log.error("로그인 요청 JSON 파싱 오류", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        try {
            // UserDetails
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

            String username = customUserDetails.getUsername();
            UUID publicId = customUserDetails.getPublicId();

            log.info("successfulAuthentication > username : {}", username);
            log.info("successfulAuthentication > publicId : {}", publicId);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();

            // 권한 획득
            String role = auth.getAuthority();

            // Authorization
            String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofMinutes(10), publicId, role);
            // X-Refresh-Token
            String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofHours(24), publicId, role);
            // URLEncoder.encode: 공백을 %2B 로 처리
            String cookieValue = URLEncoder.encode("Bearer " + refreshToken, StandardCharsets.UTF_8);

            log.debug("print accessToken: {}", accessToken);
            log.debug("print refreshToken: {}", refreshToken);

            //Refresh 토큰 저장
            addRefreshEntity(publicId, refreshToken, Duration.ofHours(24));

            response.addHeader(ACCESS_TOKEN_KEY, "Bearer " + accessToken);
            response.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, TOKEN_REISSUE_PATH, 24 * 60 * 60, true));
            response.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, LOGOUT_PATH, 24 * 60 * 60, true));
            response.setStatus(HttpStatus.OK.value());


//            frontUrl += "?" + ACCESS_TOKEN_KEY + "=" + ("Bearer%20" + accessToken);
//            frontUrl += "&" + REFRESH_TOKEN_KEY + "=" + ("Bearer%20" + refreshToken);
//            response.sendRedirect(frontUrl);
        log.info("자체 서비스 로그인에 성공하였습니다.");
        } catch (InternalAuthenticationServiceException e) {
            System.out.println("successfulAuthentication 메서드 에러 발생 : " + e.getMessage());
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        String message = failed.getMessage();
        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);
        log.debug("message : {}", message);
        System.out.println("fail authentication");
    }

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
}
