package org.myteam.server.global.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseStatus {
    SUCCESS(1), FAIL(0);
    private Integer value;
}
