package org.myteam.server.ban.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.myteam.server.ban.domain.Ban;
import org.myteam.server.ban.domain.BanReason;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 밴 정보를 반환하기 위한 DTO
 */
@Getter
@Builder
public class BanResponse {
    private Long id;
    private String username;
    private List<BanReason> reason;
    private String bannedAt;
}