package org.myteam.server.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.global.web.response.ResponseDto;
import org.myteam.server.member.dto.*;
import org.myteam.server.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.myteam.server.global.web.response.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoController {

    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody @Valid MemberSaveRequest memberSaveRequest) {
        log.info("MyInfoController create 메서드 실행");
        MemberResponse response = memberService.create(memberSaveRequest);
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
    public ResponseEntity<?> update(@RequestBody @Valid MemberUpdateRequest memberUpdateRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController update 메서드 실행 : {}", memberUpdateRequest.toString());
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        MemberResponse response = memberService.update(email, memberUpdateRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "회원정보 수정 성공", response), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController changePassword 메서드 실행 : {}", passwordChangeRequest.toString());
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        memberService.changePassword(email, passwordChangeRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "비밀번호 변경 성공", null), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> delete(@PathVariable String email, @RequestBody MemberDeleteRequest memberDeleteRequest) {
        log.info("MyInfoController delete 메서드 실행");
        memberService.delete(email, memberDeleteRequest.getPassword());
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.name(), "회원 삭제 성공", null), HttpStatus.OK);
    }
}
