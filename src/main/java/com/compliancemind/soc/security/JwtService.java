package com.compliancemind.soc.security;

import com.compliancemind.soc.common.constants.SocConstants;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * JWT 签发与校验（auth0-java-jwt），声明中包含用户 ID、展示名与角色。
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-seconds}")
    private long expireSeconds;

    public String generateToken(Integer userId, String username, String roleCode) {
        Instant now = Instant.now();
        return JWT.create()
            .withClaim(SocConstants.JwtClaim.USER_ID, userId)
            .withClaim(SocConstants.JwtClaim.USERNAME, username)
            .withClaim(SocConstants.JwtClaim.ROLE_CODE, roleCode)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(expireSeconds)))
            .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }
}

