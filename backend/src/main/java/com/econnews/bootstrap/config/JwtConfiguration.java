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
}