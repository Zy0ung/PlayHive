package org.myteam.server.ban.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 밴(Ban) 엔티티.
 * 특정 유저가 밴된 정보를 나타내며, 밴 사유와 밴된 시점 등을 포함한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // 밴 대상 사용자명

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private List<BanReason> reasons = new ArrayList<>(); // 밴 사유

    private LocalDateTime bannedAt; // 밴된 시점

    private boolean active; // 밴 활성화 플래그

    @Builder
    public Ban(String username, List<BanReason> reasons, LocalDateTime bannedAt, boolean active) {
        this.username = username;
        this.bannedAt = bannedAt;
        if (reasons != null) {
            this.reasons.addAll(reasons);
        }
        this.active = active;
    }

    /**
     * 밴 엔티티 생성
     */
    public static Ban createBan(String username, List<BanReason> reasons) {
        return Ban.builder()
                .username(username)
                .reasons(reasons)
                .bannedAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    /**
     * 밴 해제 로직
     */
    public void deactivateBan() {
        this.active = false;
    }
}
