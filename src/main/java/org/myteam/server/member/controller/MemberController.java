package org.myteam.server.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.myteam.server.member.dto.MemberResponseDto;
import org.myteam.server.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))})
    @GetMapping("/mydata")
    public ResponseEntity<MemberResponseDto> getMyData(@AuthenticationPrincipal UUID userId) {
        MemberResponseDto response = memberService.getMyData(userId);
        return ResponseEntity.ok(response);
    }
}
