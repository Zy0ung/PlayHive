package org.myteam.server.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("TokenAuthenticationFilter 토큰을 검사중");
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String accessToken = getAccessToken(authorizationHeader);

        logger.info("accessToken : " + accessToken);
        if (StringUtils.hasText(accessToken)) {
            if (jwtProvider.validToken(accessToken)) {

                //토큰에서 username과 role 획득
                UUID publicId = jwtProvider.getPublicId(accessToken);
                String role = jwtProvider.getRole(accessToken);

                logger.info("publicId : "+ publicId);
                logger.info("role : "+ role);

                //Member 를 생성하여 값 set
                Member member = Member.builder()
                        .publicId(publicId)
                        .role(MemberRole.valueOf(role))
                        .build();

                CustomUserDetails customUserDetails = new CustomUserDetails(member);

                Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("security Context 에 정보 저장이 완료되었습니다.");
            } else {
                throw new PlayHiveException(ACCESS_TOKEN_EXPIRED);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * authorization header 에서 access token 을 추출합니다.
     *
     * @param authorizationHeader : String authorization header
     * @return String access token
     */
    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.replace(TOKEN_PREFIX, "");
        }
        return null;
    }
}
