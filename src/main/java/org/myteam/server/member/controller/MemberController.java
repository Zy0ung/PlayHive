package org.myteam.server.member.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.web.response.ResponseDto;
import org.myteam.server.member.domain.MemberStatus;
import org.myteam.server.member.dto.ExistMemberRequest;
import org.myteam.server.member.controller.response.MemberResponse;
import org.myteam.server.member.dto.MemberStatusUpdateRequest;
import org.myteam.server.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.myteam.server.global.security.jwt.JwtProvider.HEADER_AUTHORIZATION;
import static org.myteam.server.global.web.response.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 이메일로 사용자 존재 여부 확인
     */
    @GetMapping("/exists/email")
    public ResponseEntity<?> existsByEmail(@Valid ExistMemberRequest existMemberRequest) {
        log.info("MemberController existsByEmail 메서드 실행 : {}", existMemberRequest.getEmail());
        boolean exists = memberService.existsByEmail(existMemberRequest.getEmail());
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.name(), "이메일 존재 여부 확인", exists));
    }

    /**
     * 닉네임으로 사용자 존재 여부 확인
     */
    @GetMapping("/exists/nickname")
    public ResponseEntity<?> existsByNickname(@Valid ExistMemberRequest existMemberRequest) {
        log.info("MemberController existsByNickname 메서드 실행 : {}", existMemberRequest.getNickname());
        boolean exists = memberService.existsByNickname(existMemberRequest.getNickname());
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.name(), "닉네임 존재 여부 확인", exists));
    }

    // TODO_ : 헤더에 publicId 를 넣어달라고 하고 받아서... 끄내고 아이디 조회해서 그걸로 다시 own 검토하면 될 것으로 보임
    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(
             @RequestBody @Valid MemberStatusUpdateRequest memberStatusUpdateRequest,
            HttpServletRequest httpServletRequest
    ) {
        log.info("MyInfoController updateStatus 메서드 실행");
        String authorizationHeader = httpServletRequest.getHeader(HEADER_AUTHORIZATION);

        // accessToken 으로 부터 유저 정보 반환
        MemberResponse response = memberService.getAuthenticatedMember(authorizationHeader);

        log.info("email : {}" , response.getEmail());

        // 서비스 호출
        MemberStatus memberStatus = memberStatusUpdateRequest.getStatus(); // 변경 상태
        String targetEmail = response.getEmail(); // 상태를 변경할 대상 이메일
        String extractedEmail = memberStatusUpdateRequest.getEmail(); // 토큰에서 추출된 이메일

        memberService.updateStatus(extractedEmail, targetEmail, memberStatus);

        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.name(), "회원 상태가 성공적으로 변경되었습니다.", null));
    }
}
