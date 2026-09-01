package com.springtest.webchatapi.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwsTokenService {

    public static final String TOKEN_TYPE = "Bearer";

    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtProperties jwtProperties;

    public JwsTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public AccessToken issueAccessToken(String userId, String username) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(jwtProperties.getIssuer())
                .audience(List.of(jwtProperties.getAudience())).subject(userId)
                .claim("username", username).claim("typ", ACCESS_TOKEN_TYPE)
                .issueTime(Date.from(issuedAt)).expirationTime(Date.from(expiresAt)).build();
        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
        try {
            signedJwt.sign(
                    new MACSigner(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to sign access token", exception);
        }
        return new AccessToken(signedJwt.serialize(), expiresAt);
    }

    public record AccessToken(String value, Instant expiresAt) {
    }

}
