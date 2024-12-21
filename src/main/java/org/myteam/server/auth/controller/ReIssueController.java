package org.myteam.server.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.auth.service.ReIssueService;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.myteam.server.global.security.jwt.JwtProvider.*;
import static org.myteam.server.util.CookieUtil.createCookie;

@Slf4j
@RestController
public class ReIssueController {
    private final JwtProvider jwtProvider;
    private final MemberService memberService;
    private final ReIssueService reIssueService;
    private static final String ACCESS_TOKEN_KEY = "Authorization";
    private static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";

    public ReIssueController(JwtProvider jwtProvider, ReIssueService reIssueService, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.reIssueService = reIssueService;
        this.memberService = memberService;
    }

    // TODO_ : 만료시간이 지난 토큰은 주기적으로 삭제하는 스케쥴러 개발 필요
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = reIssueService.extractRefreshToken(request);

        UUID publicId = jwtProvider.getPublicId(refresh);
        String role = jwtProvider.getRole(refresh);

        log.info("extracted refresh token: {}", refresh);
        log.info("extracted publicId: {}", publicId);
        log.info("extracted role: {}", role);

        reIssueService.validateRefreshToken(refresh, publicId);

        // Authorization
        String newAccess = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofMinutes(10), publicId, role);
        // X-Refresh-Token
        String newRefresh = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofHours(24), publicId, role);
        // URLEncoder.encode: 공백을 %2B 로 처리
        String cookieValue = URLEncoder.encode("Bearer " + newRefresh, StandardCharsets.UTF_8);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        reIssueService.deleteByRefreshAndPublicId(refresh, publicId);
        reIssueService.addRefreshEntity(publicId, newRefresh, Duration.ofHours(24));

        //response
        response.addHeader(ACCESS_TOKEN_KEY, "Bearer " + newAccess);
        // 리프레시 토큰에 대한 블랙 리스트 작성
        response.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, 24 * 60 * 60, true));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}