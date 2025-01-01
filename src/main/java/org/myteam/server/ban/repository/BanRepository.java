package org.myteam.server.ban.repository;

import org.myteam.server.ban.domain.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {

    Optional<Ban> findByUsername(String username);

    boolean existsByUsername(String username);
}
