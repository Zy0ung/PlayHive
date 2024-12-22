package org.myteam.server.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.auth.domain.Tokens;
import org.myteam.server.auth.service.ReIssueService;
import org.myteam.server.global.exception.PlayHiveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.myteam.server.util.CookieUtil.createCookie;

/**
 * TODO_ : 리프레시 토큰에 대한 블랙 리스트 작성
 */
@Slf4j
@RestController
public class ReIssueController {
    private final ReIssueService reIssueService;
    private static final String ACCESS_TOKEN_KEY = "Authorization";
    private static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
    public final static String TOKEN_PREFIX = "Bearer ";
    public final static String TOKEN_REISSUE_PATH = "/reissue";

    public ReIssueController(ReIssueService reIssueService) {
        this.reIssueService = reIssueService;
    }

    // TODO_ : 만료시간이 지난 토큰은 주기적으로 삭제하는 스케쥴러 개발 필요
    @PostMapping(TOKEN_REISSUE_PATH)
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("ReIssueController reissue START");

        try {
            // 서비스 호출로 모든 로직을 위임
            Tokens tokens = reIssueService.reissueTokens(request);

            // Access Token 응답 헤더 추가
            response.addHeader(ACCESS_TOKEN_KEY, TOKEN_PREFIX + tokens.getAccessToken());

            // Refresh Token 쿠키 추가
            response.addCookie(createCookie(
                    REFRESH_TOKEN_KEY,
                    URLEncoder.encode(TOKEN_PREFIX + tokens.getRefreshToken(), StandardCharsets.UTF_8),
                    TOKEN_REISSUE_PATH,
                    24 * 60 * 60,
                    true
            ));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (PlayHiveException e) {
            // 서비스에서 발생한 PlayHiveException 을 그대로 재던짐
            log.error("서비스에서 발생한 PlayHiveException");
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            // 일반 예외는 PlayHiveException 으로 에러를 던짐
            log.error("Unexpected error during token reissue :" + e.getMessage());
            throw new PlayHiveException(e.getMessage());
        }
    }
}