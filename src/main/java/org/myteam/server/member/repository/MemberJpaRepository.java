package org.myteam.server.member.repository;

import java.util.Optional;
import java.util.UUID;

import org.myteam.server.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByPublicId(UUID publicId);
}
