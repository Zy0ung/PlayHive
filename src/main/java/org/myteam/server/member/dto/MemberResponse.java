package org.myteam.server.member.dto;

import lombok.Builder;
import lombok.Getter;
import org.myteam.server.member.domain.GenderType;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.domain.MemberType;
import org.myteam.server.member.entity.Member;

import java.time.LocalDate;

@Getter
public class MemberResponse {
    private Long id;

    private String email; // 계정

    private String tel;

    private String name;

    private String nickname;

    private GenderType gender;

    private LocalDate birthdate;

    private MemberRole role;

    private MemberType type;

    @Builder
    public MemberResponse(final Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.tel = member.getTel();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.gender = member.getGender();
        this.birthdate = member.getBirthdate();
        this.role = member.getRole();
        this.type = member.getType();
    }
}
