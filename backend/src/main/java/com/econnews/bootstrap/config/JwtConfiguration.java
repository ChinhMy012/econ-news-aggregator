package com.econnews.bootstrap.config;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.econnews.auth.adapter.out.security.JwtAccessTokenIssuer;
import com.econnews.auth.application.port.out.AccessTokenIssuer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

//Đọc khóa và nối các bean
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    @Bean
    public JwtEncoder jwtEncoder(
            JwtProperties properties) throws IOException {

        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

        try (var input = properties.privateKey().getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(input);
        }

        try (var input = properties.publicKey().getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }

        if (privateKey == null || publicKey == null) {
            throw new IllegalStateException(
                    "Không đọc được cặp khóa RSA.");
        }

        if (!privateKey.getModulus().equals(publicKey.getModulus())) {
            throw new IllegalStateException(
                    "Khóa riêng và khóa công khai không cùng cặp RSA.");
        }

        if (publicKey.getModulus().bitLength() < 2048) {
            throw new IllegalStateException(
                    "Khóa RSA phải có ít nhất 2048 bit.");
        }

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(properties.keyId())
                .build();

        var keySource = new ImmutableJWKSet<SecurityContext>(
                new JWKSet(rsaKey));

        return new NimbusJwtEncoder(keySource);
    }

    @Bean
    public AccessTokenIssuer accessTokenIssuer(
            JwtEncoder encoder,
            JwtProperties properties) {

        Duration ttl = properties.accessTokenTtl();

        if (ttl.toSeconds() < 1) {
            throw new IllegalStateException(
                    "Thời hạn access token phải từ một giây trở lên.");
        }

        return new JwtAccessTokenIssuer(
                encoder,
                Clock.systemUTC(),
                properties.issuer(),
                properties.audience(),
                properties.keyId(),
                ttl);
    }

    // Chỉ xác minh token; SecurityFilterChain sẽ sử dụng bean này ở bước tiếp theo.
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) throws IOException {
        RSAPublicKey publicKey;
        try (var input = properties.publicKey().getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }
        if (publicKey == null) {
            throw new IllegalStateException("Không đọc được khóa công khai RSA.");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(properties.issuer());
        OAuth2TokenValidator<Jwt> applicationValidator =
                jwt -> validateAccessToken(jwt, properties);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                defaultValidator, applicationValidator));
        return decoder;
    }

    private OAuth2TokenValidatorResult validateAccessToken(
            Jwt jwt, JwtProperties properties) {
        if (jwt.getExpiresAt() == null || jwt.getIssuedAt() == null
                || jwt.getNotBefore() == null) {
            return invalidToken("Token thiếu thông tin thời gian bắt buộc.");
        }
        if (!jwt.getExpiresAt().isAfter(jwt.getIssuedAt())) {
            return invalidToken("Thời gian hết hạn không hợp lệ.");
        }
        if (jwt.getAudience() == null
                || !jwt.getAudience().contains(properties.audience())) {
            return invalidToken("Token không dành cho API này.");
        }
        if (!"access".equals(jwt.getClaimAsString("token_type"))) {
            return invalidToken("Loại token không được chấp nhận.");
        }
        String subject = jwt.getSubject();
        try {
            if (subject == null || Long.parseLong(subject) <= 0) {
                return invalidToken("Định danh tài khoản không hợp lệ.");
            }
        } catch (NumberFormatException exception) {
            return invalidToken("Định danh tài khoản không hợp lệ.");
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult invalidToken(String description) {
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", description, null));
    }
}