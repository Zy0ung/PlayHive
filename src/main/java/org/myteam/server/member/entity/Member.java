package org.myteam.server.member.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.member.domain.GenderType;
import org.myteam.server.member.domain.MemberRole;
import org.myteam.server.member.domain.MemberType;
import org.myteam.server.member.dto.MemberSaveRequest;
import org.myteam.server.member.dto.MemberUpdateRequest;
import org.myteam.server.member.dto.PasswordChangeRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.myteam.server.member.domain.MemberRole.USER;
import static org.myteam.server.member.domain.MemberType.LOCAL;

@Slf4j
@Entity
@Getter
@Table(name = "p_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // 계정

    @Column(nullable = false, length = 60) // 패스워드 인코딩(BCrypt)
    private String password; // 비밀번호

    @Column(nullable = false, length = 11)
    private String tel;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 60)
    private String nickname;

    // YYYY-MM-dd 형식
    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Column(name = "gender")
    private GenderType gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MemberType type = LOCAL;

    @Column(name = "public_id", nullable = false, updatable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID publicId;

    @Builder
    public Member(Long id, String email, String password, String tel, String name, String nickname, LocalDate birthdate, GenderType gender, MemberRole role, MemberType type, UUID publicId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.tel = tel;
        this.name = name;
        this.nickname = nickname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.role = role;
        this.type = type;
        this.publicId = publicId;
    }

    @Builder
    public Member(MemberSaveRequest memberSaveRequest, PasswordEncoder passwordEncoder) {
        this.email = memberSaveRequest.getEmail();
        this.password = passwordEncoder.encode(memberSaveRequest.getPassword());
        this.tel = memberSaveRequest.getTel();
        this.name = memberSaveRequest.getName();
        this.nickname = memberSaveRequest.getNickname();
        this.gender = GenderType.fromValue(memberSaveRequest.getGender());
        this.birthdate = memberSaveRequest.getBirthdate();
    }

    // 전체 업데이트 메서드
    public void update(MemberUpdateRequest memberUpdateRequest, PasswordEncoder passwordEncoder) {
        // this.email = memberUpdateRequest.getEmail();
        // this.password = passwordEncoder.encode(memberUpdateRequest.getPassword()); // 비밀번호 변경 시 암호화 필요
        this.name = memberUpdateRequest.getName();
        this.tel = memberUpdateRequest.getTel();
        this.nickname= memberUpdateRequest.getNickname();
        this.gender = GenderType.fromValue(memberUpdateRequest.getGender());
        this.birthdate = memberUpdateRequest.getBirthdate();
    }

    public void updatePassword(PasswordChangeRequest passwordChangeRequest, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(passwordChangeRequest.getPassword()); // 비밀번호 변경 시 암호화 필요
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public boolean verifyOwnEmail(String email) {
        return email.equals(this.email);
    }

    public boolean validatePassword(String inputPassword, PasswordEncoder bCryptPasswordEncoder) {
        // 입력된 평문 패스워드와 이미 암호화된 패스워드를 비교
        boolean isValid = bCryptPasswordEncoder.matches(inputPassword, this.password);
        log.info("Input password: {}", inputPassword);
        log.info("Stored password: {}", this.password);
        log.info("Is valid: {}", isValid);
        return isValid;
    }
}