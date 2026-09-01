package com.springtest.webchatapi.security;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.springtest.webchatapi.exception.UnauthorizedException;

@Service
public class JwsTokenValidator {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtProperties jwtProperties;

    public JwsTokenValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public AuthenticatedUser validateAuthorizationHeader(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.regionMatches(true, 0,
                BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new UnauthorizedException("Missing bearer token");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("Missing bearer token");
        }
        return validate(token);
    }

    public AuthenticatedUser validate(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(
                    new MACVerifier(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))) {
                throw new UnauthorizedException("Invalid token signature");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            validateClaims(claims);
            return new AuthenticatedUser(claims.getSubject(), claims.getStringClaim("username"));
        } catch (ParseException | JOSEException exception) {
            throw new UnauthorizedException("Invalid token", exception);
        }
    }

    private void validateClaims(JWTClaimsSet claims) throws ParseException {
        if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
            throw new UnauthorizedException("Invalid token issuer");
        }
        if (claims.getAudience() == null
                || !claims.getAudience().contains(jwtProperties.getAudience())) {
            throw new UnauthorizedException("Invalid token audience");
        }
        if (!ACCESS_TOKEN_TYPE.equals(claims.getStringClaim("typ"))) {
            throw new UnauthorizedException("Invalid token type");
        }
        if (!StringUtils.hasText(claims.getSubject())) {
            throw new UnauthorizedException("Missing token subject");
        }
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || !expirationTime.toInstant().isAfter(Instant.now())) {
            throw new UnauthorizedException("Token expired");
        }
    }

    public record AuthenticatedUser(String userId, String username) {
    }
}
