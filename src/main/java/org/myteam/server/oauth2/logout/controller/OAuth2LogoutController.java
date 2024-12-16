package org.myteam.server.oauth2.logout.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.oauth2.constant.OAuth2ServiceProvider;
import org.myteam.server.oauth2.logout.util.OAuth2LogoutHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기능 미구현 상태. 추후 구현 계획이 확실해 지면 그 때 추가 계발 계획 예정
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Deprecated
public class OAuth2LogoutController {

    private final OAuth2LogoutHelper oAuth2LogoutHelper;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println(authentication.getPrincipal()); // 계정
        System.out.println(authentication.getAuthorities()); // 권한
        System.out.println(authentication.getCredentials()); // 토큰

        if (authentication != null) {
            // Authorization 헤더에서 값 추출
            String authorizationHeader = httpServletRequest.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.replace("Bearer ", "");
                oAuth2LogoutHelper.revokeToken(OAuth2ServiceProvider.NAVER, accessToken);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Authorization 헤더가 없거나 올바르지 않습니다.");
            }

            return ResponseEntity.ok("로그아웃이 정상적으로 처리되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자 입니다");
    }
}
