package org.myteam.server.ban.service;

import lombok.AllArgsConstructor;
import org.myteam.server.ban.domain.Ban;
import org.myteam.server.ban.dto.request.BanRequest;
import org.myteam.server.ban.dto.response.BanResponse;
import org.myteam.server.ban.repository.BanRepository;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
public class BanService {

    private final BanRepository banRepository;

    /**
     * 유저 밴 적용
     */
    public BanResponse banUser(BanRequest request) {
        // 이미 밴된 유저인지 확인
        if (banRepository.existsByUsername(request.getUsername())) {
            throw new PlayHiveException(ErrorCode.BAN_ALREADY_EXISTS);
        }

        Ban ban = Ban.createBan(request.getUsername(), request.getReasons());
        Ban savedBan = banRepository.save(ban);

        return toBanResponse(savedBan);
    }

    /**
     * 유저 밴 해제 (삭제)
     */
    public String unbanUser(String username) {
        Ban ban = banRepository.findByUsername(username)
                .orElseThrow(() -> new PlayHiveException(ErrorCode.BAN_NOT_FOUND));

        banRepository.delete(ban);

        return ban.getUsername();
    }

    /**
     * 특정 유저 밴 정보 조회
     */
    public BanResponse findBanByUsername(String username) {
        Ban ban = banRepository.findByUsername(username)
                .orElseThrow(() -> new PlayHiveException(ErrorCode.BAN_NOT_FOUND));

        return toBanResponse(ban);
    }

    private BanResponse toBanResponse(Ban ban) {
        return BanResponse.builder()
                .id(ban.getId())
                .username(ban.getUsername())
                .reason(ban.getReasons())
                .bannedAt(ban.getBannedAt().format(DateTimeFormatter.ofPattern("YYYY-MM-DD")))
                .build();
    }
}
