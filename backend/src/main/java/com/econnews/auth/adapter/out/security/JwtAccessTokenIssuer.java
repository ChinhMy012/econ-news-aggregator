package com.econnews.auth.adapter.out.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import com.econnews.auth.application.model.AuthenticatedAdmin;
import com.econnews.auth.application.model.IssuedAccessToken;
import com.econnews.auth.application.port.out.AccessTokenIssuer;

//Adapter ký token
public final class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;
    private final Clock clock;
    private final String issuer;
    private final String audience;
    private final String keyId;
    private final Duration ttl;

    public JwtAccessTokenIssuer(
            JwtEncoder encoder,
            Clock clock,
            String issuer,
            String audience,
            String keyId,
            Duration ttl) {
        this.encoder = encoder;
        this.clock = clock;
        this.issuer = issuer;
        this.audience = audience;
        this.keyId = keyId;
        this.ttl = ttl;
    }

    @Override
    public IssuedAccessToken issue(AuthenticatedAdmin admin) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(admin.id().toString())
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ADMIN"))
                .claim("token_type", "access")
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .type("JWT")
                .keyId(keyId)
                .build();

        String token = encoder.encode(
                JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedAccessToken(
                token,
                ttl.toSeconds(),
                expiresAt);
    }
}