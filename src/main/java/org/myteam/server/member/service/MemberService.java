package org.myteam.server.member.service;

import lombok.RequiredArgsConstructor;
import org.myteam.server.member.dto.MemberSaveRequest;
import org.myteam.server.member.dto.MemberResponse;
import org.myteam.server.member.dto.MemberUpdateRequest;
import org.myteam.server.member.dto.PasswordChangeRequest;
import org.myteam.server.member.entity.Member;
import org.myteam.server.member.repository.MemberJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse create(MemberSaveRequest memberSaveRequest) {
        // 1. 동일한 유저 이름 존재 검사
        Optional<Member> memberOP = memberJpaRepository.findByEmail(memberSaveRequest.getEmail());

        if (memberOP.isPresent()) {
            // 아이디가 중복 되었다는 것
            throw new RuntimeException("해당 계정이 이미 존재합니다.");
        }

        // 2. 패스워드인코딩 + 회원 가입
        Member member = memberJpaRepository.save(new Member(memberSaveRequest, passwordEncoder));

        // 4. dto 응답
        return new MemberResponse(member);
    }

    @Transactional
    public MemberResponse update(String email, MemberUpdateRequest memberUpdateRequest) {
        // 1. 동일한 유저 이름 존재 검사
        Optional<Member> memberOP = memberJpaRepository.findByEmail(email);

        // 2. 아이디 미존재 체크
        if (memberOP.isEmpty()) {
            throw new RuntimeException("아이디가 존재하지 않습니다.");
        }

        // 3. 자신의 계정이 아닌 다른 계정을 수정하려고 함
        if (!memberOP.get().verifyOwnEmail(email)) {
            throw new RuntimeException("자신의 계정이 아닙니다.");
        }

        // 4. 패스워드인코딩 + 회원 정보 변경
        Member member = memberOP.get();
        member.update(memberUpdateRequest, passwordEncoder);

        // 5. dto 응답
        return new MemberResponse(member);
    }

    public Member getByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(email + " 는 존재하지 않는 사용자 입니다"));
    }

    public MemberResponse getByPublicId(UUID publicId) {
        return new MemberResponse(memberJpaRepository.findByPublicId(publicId)
                        .orElseThrow(() -> new RuntimeException(publicId + " 는 존재하지 않는 PublicId 입니다")));
    }

    @Transactional
    public void delete(String email, String password) {
        Member findMember = getByEmail(email);
        boolean isValid = findMember.validatePassword(password, passwordEncoder);
        if (!isValid) throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        memberJpaRepository.delete(findMember);
    }

    public List<Member> list() {
        return Optional.of(memberJpaRepository.findAll()).orElse(Collections.emptyList());
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest passwordChangeRequest) {
        Member findMember = getByEmail(email);
        boolean isEqual = passwordChangeRequest.checkPasswordAndConfirmPassword();
        if (!isEqual) throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        boolean isValid = findMember.validatePassword(passwordChangeRequest.getPassword(), passwordEncoder);
        if (!isValid) throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");

        findMember.updatePassword(passwordChangeRequest, passwordEncoder); // 비밀번호 변경
    }

    /**
     * publicId 를 통한 사용자 아이디 조회
     *
     * @param publicId token 에 저장할 고유 번호
     * @return
     */
    public String getCurrentLoginUserEmail(UUID publicId) {
        MemberResponse response = getByPublicId(publicId);
        return response != null ? response.getEmail() : null;
    }
}
