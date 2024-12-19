package org.myteam.server.global.security.service;

import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.security.dto.CustomUserDetails;
import org.myteam.server.member.entity.Member;
import org.myteam.server.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository userRepository;

    public CustomUserDetailsService(MemberRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("일반 로그인 CustomUserDetailsService 실행됨");
        log.info("username : {}", username);
        Optional<Member> memberOP = userRepository.findByEmail(username);

        if (memberOP.isPresent()) {
            log.info("유저가 존재합니다. 인증 처리 로직을 실행합니다.");
            return new CustomUserDetails(memberOP.get());
        }

        log.info("사용자를 찾을수 없습니다.");
        return null;
    }
}