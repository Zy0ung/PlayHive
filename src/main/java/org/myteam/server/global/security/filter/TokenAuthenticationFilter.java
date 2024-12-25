package org.myteam.server.global.security.filter;

import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.myteam.server.global.exception.ErrorCode.*;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_ACCESS;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("TokenAuthenticationFilter 토큰을 검사중");
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String accessToken = jwtProvider.getAccessToken(authorizationHeader);

        log.info("accessToken : " + accessToken);
        if (StringUtils.isNotBlank(accessToken)) {
            if (jwtProvider.validToken(accessToken)) {

                String accessCategory = jwtProvider.getCategory(accessToken);

                if (!accessCategory.equals(TOKEN_CATEGORY_ACCESS)) {
                    // RestControllerAdvice 로 에러가 전달 되지 않아 여기서 에러 처리함
                    log.warn("잘못된 토큰 유형입니다.");
                    sendErrorResponse(response, INVALID_TOKEN_TYPE.getStatus(), "잘못된 토큰 유형");
                    return;
                }

                // 토큰에서 username과 role 획득
                UUID publicId = jwtProvider.getPublicId(accessToken);
                String role = jwtProvider.getRole(accessToken);

                log.info("publicId : " + publicId);
                log.info("role : " + role);

                // Member 를 생성하여 값 set
                Member member = Member.builder()
                        .publicId(publicId)
                        .role(MemberRole.valueOf(role))
                        .build();

                CustomUserDetails customUserDetails = new CustomUserDetails(member);

                Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("security Context 에 정보 저장이 완료되었습니다.");
            } else {
                try {
                    if (jwtProvider.isExpired(accessToken)) {
                        // RestControllerAdvice 로 에러가 전달 되지 않아 여기서 에러 처리함
                        log.warn("토큰이 만료되었습니다.");
                        sendErrorResponse(response, ACCESS_TOKEN_EXPIRED.getStatus(), "만료된 토큰");
                        return;
                    }
                } catch (JwtException | IllegalArgumentException e) {
                    // RestControllerAdvice 로 에러가 전달 되지 않아 여기서 에러 처리함
                    log.error("잘못된 JWT 토큰 형식 또는 그 외 에러 : {}", e.getMessage());
                    sendErrorResponse(response, INVALID_ACCESS_TOKEN.getStatus(), "잘못된 JWT 토큰 형식 또는 그 외 에러");
                    return;
                }
                // RestControllerAdvice 로 에러가 전달 되지 않아 여기서 에러 처리함
                log.warn("인증되지 않은 토큰입니다.");
                sendErrorResponse(response, INVALID_ACCESS_TOKEN.getStatus(), "인증되지 않은 토큰");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 공통 에러 응답 처리 메서드
     *
     * @param response HttpServletResponse
     * @param httpStatus HTTP 상태 오브젝트
     * @param message 메시지
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"message\":\"%s\",\"status\":\"%s\"}", message, httpStatus.name()));
    }
}
