package org.myteam.server.ban.domain;

public enum BanReason {
    SPAM("광고성 채팅"),
    HARASSMENT("비방성 채팅"),
    OFFENSIVE_LANGUAGE("공격적인 채팅"),
    ETC("기타");

    private String reason;
    BanReason(String reason) {
        this.reason = reason;
    }
}
