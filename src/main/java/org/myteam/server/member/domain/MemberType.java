package org.myteam.server.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberType {
    GENERAL("로컬사용자"), SOCIAL("소셜회원");
    private String value;
}
