package org.myteam.server.member.entity;

import jakarta.persistence.*;

import java.util.UUID;

import lombok.*;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.domain.MemberType;

import static org.myteam.server.member.domain.MemberRole.USER;
import static org.myteam.server.member.domain.MemberType.LOCAL;

@Entity
@Table(name = "p_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MemberType type = LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    //TODO: 적은 확률로 충돌이 일어나는 상황을 방지하는 로직 필요
    @Column(name = "public_id", nullable = false, updatable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID publicId = UUID.randomUUID();

    @Builder
    private Member(String username, String name, String password, String email, MemberRole role, MemberType type, String providerId) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
        this.type = type;
        this.providerId = providerId;
    }

    @Builder
    private Member(String username, String name, String password, String email, MemberRole role) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    @Builder
    private Member(String username, String password, String email, MemberRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}