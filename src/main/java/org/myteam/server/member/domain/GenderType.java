package org.myteam.server.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.myteam.server.global.exception.PlayHiveException;

import static org.myteam.server.global.exception.ErrorCode.INVALID_PARAMETER;

@AllArgsConstructor
@Getter
public enum GenderType {
    M("MALE"), F("FEMALE");
    private String value;

    public static GenderType fromValue(String value) {
        for (GenderType gender : GenderType.values()) {
            if (gender.getValue().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new PlayHiveException(INVALID_PARAMETER, "Invalid gender value: " + value);
    }
}
