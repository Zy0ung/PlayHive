package org.myteam.server.global.exception;

import lombok.Getter;

import java.util.Optional;

@Getter
public class PlayHiveException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * @param errorCode ErrorCode에 정의된 메시지 반환
     */
    public PlayHiveException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * @param message 정의되지 않은 예외 처리
     */
    public PlayHiveException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNSUPPORTED_OAUTH_PROVIDER;
    }
}
