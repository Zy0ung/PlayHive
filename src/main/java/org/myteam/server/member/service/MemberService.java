package org.myteam.server.member.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.myteam.server.member.dto.MemberResponseDto;
import org.myteam.server.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getMyData(UUID userId) {
        return MemberResponseDto.from(memberRepository.getByPublicId(userId).orElseThrow(
                () -> new RuntimeException("Member not found")));
    }
}
