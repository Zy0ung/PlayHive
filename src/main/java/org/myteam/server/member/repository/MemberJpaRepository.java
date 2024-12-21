package org.myteam.server.member.repository;

import java.util.Optional;
import java.util.UUID;

import org.myteam.server.member.domain.MemberType;
import org.myteam.server.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    // List<Member> findMembersByEmail(String email);
    // Optional<Member> findByEmailAndPublicId(String email, UUID publicId);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByEmailAndType(String email, MemberType type);
    Optional<Member> findByPublicId(UUID publicId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
