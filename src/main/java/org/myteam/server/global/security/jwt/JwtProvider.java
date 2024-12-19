package org.myteam.server.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    /**
     * 토큰 발급
     *
     * @param duration Duration 만료 기간
     * @param publicId   UUID
     * @param role     String
     * @return String
     */
    public String generateToken(Duration duration, UUID publicId, String role) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + duration.toMillis()), publicId, role);
    }

    /**
     * 토큰 생성
     *
     * @param expirationDate Date 만료 시간
     * @param publicId         UUID
     * @param role           String
     * @return String
     */
    private String makeToken(Date expirationDate, UUID publicId, String role) {
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(expirationDate)
                .claim("id", publicId)
                .claim("role", role)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token String
     * @return boolean
     */
    public boolean validToken(final String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 Authentication 객체를 가져옴
     *
     * @param token String
     * @return Authentication
     */
    public Authentication getAuthentication(final String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)));
        return new UsernamePasswordAuthenticationToken(UUID.fromString(claims.get("id", String.class)), token,
                authorities);
    }

    /**
     * 토큰으로부터 사용자 publicId를 추출
     *
     * @param token String
     * @return UUID
     */
    public UUID getPublicId(final String token) {
        Claims claims = getClaims(token);
        String idString = claims.get("id", String.class);
        return UUID.fromString(idString);
    }

    /**
     * 토큰으로부터 사용자 권한(Authorities)을 추출
     *
     * @param token String
     * @return Set<SimpleGrantedAuthority>
     */
    public String getRole(final String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰으로부터 Claims를 가져옴
     *
     * @param token String
     * @return Claims
     */
    private Claims getClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
        return claimsJws.getPayload();
    }

    /**
     * 서명 키 생성
     *
     * @return SecretKey
     */
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
    }
}