package org.myteam.server.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.member.entity.Member;
import org.myteam.server.member.repository.MemberRepository;
import org.myteam.server.oauth2.dto.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@Component
public class CustomOauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${app.frontend.url.${spring.profiles.active}}")
    private String frontendUrl;
    private static final String ACCESS_TOKEN_KEY = "Authorization";
    public static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    public CustomOauth2SuccessHandler(JwtProvider jwtProvider, MemberRepository memberRepository) {
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("onAuthenticationSuccess : Oauth 로그인 성공");
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();
        log.info("onAuthenticationSuccess username: {}", username);
        log.info("onAuthenticationSuccess role: {}", role);
        //유저확인
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        log.info("onAuthenticationSuccess publicId: {}", member.getPublicId());
        log.info("onAuthenticationSuccess role: {}", member.getRole());
        // Authorization
        String accessToken = jwtProvider.generateToken(Duration.ofHours(1), member.getPublicId(), member.getRole().toString());
        // X-Refresh-Token
        String refreshToken = jwtProvider.generateToken(Duration.ofDays(7), member.getPublicId(), member.getRole().toString());


        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam(ACCESS_TOKEN_KEY, accessToken)
                .queryParam(REFRESH_TOKEN_KEY, refreshToken)
                .build().toUriString();

        log.debug("Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.setHeader(ACCESS_TOKEN_KEY, accessToken);
//        response.setHeader(REFRESH_TOKEN_KEY, refreshToken);
//        response.getWriter().write("accessToken = " + accessToken);
//        response.getWriter().write("\n");
//        response.getWriter().write("refreshToken = " + refreshToken);
//        response.getWriter().write("\n");
//        response.getWriter().write("boolean = " + jwtProvider.validToken(accessToken));
//        response.getWriter().write("\n");
        log.debug("Oauth 로그인에 성공하였습니다.");
    }
}
