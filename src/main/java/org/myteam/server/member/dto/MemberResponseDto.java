package org.myteam.server.member.dto;

import lombok.Builder;
import lombok.Getter;
import org.myteam.server.member.entity.Member;

@Getter
@Builder
public class MemberResponseDto {
    private final Long id;
    private final String name;
    private final String email;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .name(member.getUsername())
                .email(member.getEmail())
                .build();
    }
}
