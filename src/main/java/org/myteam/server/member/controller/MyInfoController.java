package org.myteam.server.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.global.web.response.ResponseDto;
import org.myteam.server.member.controller.response.MemberResponse;
import org.myteam.server.member.dto.*;
import org.myteam.server.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.myteam.server.auth.controller.ReIssueController.TOKEN_REISSUE_PATH;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_ACCESS;
import static org.myteam.server.global.security.jwt.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static org.myteam.server.global.web.response.ResponseStatus.SUCCESS;
import static org.myteam.server.util.CookieUtil.createCookie;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoController {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    private static final String ACCESS_TOKEN_KEY = "Authorization";
    private static final String REFRESH_TOKEN_KEY = "X-Refresh-Token";

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid MemberSaveRequest memberSaveRequest,
                                    BindingResult bindingResult,
                                    HttpServletResponse httpServletResponse
    ) {
        log.info("MyInfoController create 메서드 실행");
        MemberResponse response = memberService.create(memberSaveRequest);

        // Authorization
        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofMinutes(10), response.getPublicId(), response.getRole().name());
        // X-Refresh-Token
        String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofHours(24), response.getPublicId(), response.getRole().name());
        // URLEncoder.encode: 공백을 %2B 로 처리
        String cookieValue = URLEncoder.encode("Bearer " + refreshToken, StandardCharsets.UTF_8);

        // 응답 헤더 설정
        httpServletResponse.addHeader(ACCESS_TOKEN_KEY, "Bearer " + accessToken);
        httpServletResponse.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, TOKEN_REISSUE_PATH, 24 * 60 * 60, true));
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "회원가입 성공", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> get(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController get 메서드 실행");
        log.info("publicId : {}", userDetails.getPublicId());

        UUID publicId = userDetails.getPublicId();
        MemberResponse response = memberService.getByPublicId(publicId);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "로그인 회원 정보 조회 성공", response), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody @Valid MemberUpdateRequest memberUpdateRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController update 메서드 실행 : {}", memberUpdateRequest.toString());
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        MemberResponse response = memberService.update(email, memberUpdateRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "회원정보 수정 성공", response), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest,
                                            BindingResult bindingResult,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController changePassword 메서드 실행 : {}", passwordChangeRequest.toString());
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        memberService.changePassword(email, passwordChangeRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "비밀번호 변경 성공", null), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody @Valid MemberDeleteRequest memberDeleteRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController delete 메서드 실행");
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일

        memberService.delete(email, memberDeleteRequest.getPassword());

        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "회원 삭제 성공", null), HttpStatus.OK);
    }
}
