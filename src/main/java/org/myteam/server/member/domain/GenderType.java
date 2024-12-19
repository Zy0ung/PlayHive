package org.myteam.server.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.myteam.server.global.exception.PlayHiveException;

@AllArgsConstructor
@Getter
public enum GenderType {
    MALE("MALE"), FEMALE("FEMALE");
    private String value;

    public static GenderType fromValue(String value) {
        for (GenderType gender : GenderType.values()) {
            if (gender.getValue().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new PlayHiveException("Invalid gender value: " + value);
    }
}
