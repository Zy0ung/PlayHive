package org.myteam.server.global.security.filter;

import io.micrometer.common.util.StringUtils;
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
import org.springframework.web.filter.OncePerRequestFilter;

import static org.myteam.server.global.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static org.myteam.server.global.exception.ErrorCode.INVALID_ACCESS_TOKEN;
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
                    // 프론트 단에서 AccessCategory 가 넘어오지 않고
                    // refreshToken 을 넘겨서 AccessToken 을 재발급 받으려는 상황
                    throw new PlayHiveException(ACCESS_TOKEN_EXPIRED);
                }

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
                throw new PlayHiveException(INVALID_ACCESS_TOKEN);
            }
        }
        filterChain.doFilter(request, response);
    }
}
