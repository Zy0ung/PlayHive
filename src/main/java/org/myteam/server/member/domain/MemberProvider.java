package org.myteam.server.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;

@AllArgsConstructor
@Getter
public enum MemberProvider {
    NAVER("naver"), KAKAO("kakao"), GOOGLE("google"), DISCORD("discord");
    private String value;

    // value로 MemberProvider를 찾는 메소드 추가
    public static MemberProvider fromValue(String value) {
        for (MemberProvider provider : values()) {
            if (provider.value.equals(value)) {
                return provider;
            }
        }
        throw new PlayHiveException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, value);
    }
}
