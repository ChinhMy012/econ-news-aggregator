package com.econnews.bootstrap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.econnews.auth.adapter.in.security.AuthRateLimitFilter;
import com.econnews.auth.adapter.in.security.SecurityErrorWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import com.econnews.auth.adapter.in.security.RestAccessDeniedHandler;
import com.econnews.auth.adapter.in.security.RestAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableConfigurationProperties(AuthRateLimitProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder decoder,
            RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler deniedHandler,
            Environment environment, AuthSessionProperties properties,
            AuthRateLimitProperties rateLimits, SecurityErrorWriter errorWriter) throws Exception {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter authentication = new JwtAuthenticationConverter();
        authentication.setJwtGrantedAuthoritiesConverter(authorities);

        CookieCsrfTokenRepository csrfCookies = new CookieCsrfTokenRepository();
        csrfCookies.setCookieCustomizer(cookie -> cookie.httpOnly(true)
                .secure(properties.cookieSecure()).sameSite("Lax").path("/"));
        DefaultBearerTokenResolver bearerTokens = new DefaultBearerTokenResolver();

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.requestCache(new NullRequestCache()))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfCookies)
                // Chỉ miễn CSRF cho API nhận Bearer header; KHÔNG miễn các POST dùng cookie.
                .ignoringRequestMatchers("/api/v1/auth/me", "/api/v1/admin/**"))
            .authorizeHttpRequests(authorize -> {
                authorize.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/login",
                        "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll();
                authorize.requestMatchers("/api/v1/admin/**", "/api/v1/auth/me").hasRole("ADMIN");
                authorize.requestMatchers(HttpMethod.GET,
                        "/api/v1/sources", "/api/v1/articles", "/api/v1/events", "/api/v1/events/*",
                        "/api/v1/events/*/milestones", "/api/v1/events/*/articles",
                        "/api/v1/events/*/documents").permitAll();
                if (environment.acceptsProfiles(Profiles.of("dev"))) {
                    authorize.requestMatchers("/api/v1/dev/diagnostics/**").permitAll();
                }
                authorize.anyRequest().denyAll();
            })
            .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint)
                    .accessDeniedHandler(deniedHandler))
            .oauth2ResourceServer(resource -> resource
                // Các endpoint này dùng mật khẩu/refresh cookie/CSRF, không dùng Bearer cũ.
                .bearerTokenResolver(request -> {
                    String path = request.getServletPath();
                    if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh")
                            || path.equals("/api/v1/auth/logout") || path.equals("/api/v1/auth/csrf")) return null;
                    return bearerTokens.resolve(request);
                })
                .jwt(jwt -> jwt.decoder(decoder).jwtAuthenticationConverter(authentication))
                .authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler));
        http.addFilterBefore(new AuthRateLimitFilter(rateLimits, errorWriter),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
