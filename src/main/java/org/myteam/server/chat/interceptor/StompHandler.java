package org.myteam.server.chat.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {

        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == headerAccessor.getCommand()) {
            String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));

            if (authorizationHeader == null || !authorizationHeader.startsWith("[Bearer ")) {
                log.warn("No Authorization header or not Bearer type");
                throw new PlayHiveException(ErrorCode.MISSING_AUTH_HEADER);
            }

            String token = authorizationHeader.split(" ")[1];
            token = token.substring(0, token.length() - 1);
            if (!jwtProvider.validToken(token)) {
                log.warn("InValid JWT token");
                throw new PlayHiveException(ErrorCode.INVALID_TOKEN);
            }
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            headerAccessor.setUser(authentication);
        }

        if (headerAccessor.equals(StompCommand.ERROR)) {
            throw new PlayHiveException(ErrorCode.INVALID_TOKEN);
        }


        return message;
    }
}
