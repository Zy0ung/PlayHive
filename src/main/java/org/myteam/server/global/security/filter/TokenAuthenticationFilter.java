package org.myteam.server.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.exception.PlayHiveException;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.myteam.server.global.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static org.myteam.server.global.exception.ErrorCode.INVALID_TOKEN;

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
        if (StringUtils.hasText(accessToken)) {
            if (jwtProvider.validToken(accessToken)) {

                //토큰에서 username과 role 획득
                UUID publicId = jwtProvider.getPublicId(accessToken);
                String role = jwtProvider.getRole(accessToken);

                log.info("publicId : "+ publicId);
                log.info("role : "+ role);

                //Member 를 생성하여 값 set
                Member member = Member.builder()
                        .publicId(publicId)
                        .role(MemberRole.valueOf(role))
                        .build();

                CustomUserDetails customUserDetails = new CustomUserDetails(member);

                Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("security Context 에 정보 저장이 완료되었습니다.");
            } else {
                if (jwtProvider.isExpired(accessToken)) {
                    // 토큰이 만료된 경우
                    throw new PlayHiveException(ACCESS_TOKEN_EXPIRED);
                }
                throw new PlayHiveException(INVALID_TOKEN);
            }
        }
        filterChain.doFilter(request, response);
    }
}
