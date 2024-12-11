package org.myteam.server.member.repository;

import java.util.Optional;
import java.util.UUID;
import org.myteam.server.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Optional<Member> getByPublicId(UUID userId);
}
