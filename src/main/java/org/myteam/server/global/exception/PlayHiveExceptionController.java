package org.myteam.server.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PlayHiveExceptionController {

    @ExceptionHandler(value = {PlayHiveException.class})
    public ResponseEntity<?> serviceException(PlayHiveException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode().getStatus(), e.getMessage()));
    }
}
