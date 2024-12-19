package org.myteam.server.member.repository;

import org.myteam.server.member.dto.MemberResponse;
import org.myteam.server.member.dto.MemberSaveRequest;
import org.myteam.server.member.dto.MemberUpdateRequest;
import org.myteam.server.member.dto.PasswordChangeRequest;
import org.myteam.server.member.entity.Member;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {
    MemberResponse create(MemberSaveRequest memberSaveRequest);

    MemberResponse update(String email, MemberUpdateRequest memberUpdateRequest);

    Member getByEmail(String email);

    MemberResponse getByPublicId(UUID publicId);

    void delete(String email, String password);

    List<Member> list();

    void changePassword(String email, PasswordChangeRequest passwordChangeRequest);
}
