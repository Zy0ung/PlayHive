package org.myteam.server.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.auth.dto.LoginRequestDto;
import org.myteam.server.auth.dto.SignupRequestDto;
import org.myteam.server.auth.dto.TokenResponseDto;
import org.myteam.server.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody LoginRequestDto requestDto) {
        log.info("login");
        return authService.login(requestDto);
    }

    @PostMapping("/signup")
    public TokenResponseDto signup(@RequestBody SignupRequestDto requestDto) {
        log.info("signup");
        return authService.signup(requestDto);
    }
}
