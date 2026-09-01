package com.springtest.webchatapi.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "webchat.jwt")
public class JwtProperties {

    private String issuer;
    private String audience;
    private String secret;
    private Duration accessTokenTtl = Duration.ofHours(24);

    @PostConstruct
    void validate() {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalStateException("webchat.jwt.issuer must not be blank");
        }
        if (!StringUtils.hasText(audience)) {
            throw new IllegalStateException("webchat.jwt.audience must not be blank");
        }
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("webchat.jwt.secret must be at least 32 bytes");
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalStateException("webchat.jwt.access-token-ttl must be positive");
        }
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

}
