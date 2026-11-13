package com.bancolombia.training.accounts.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/** Resource Server reactivo. La seguridad se activa solo con el perfil lab03. */
@Configuration
@Profile("lab03")
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain accountSecurity(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // API stateless: solo Bearer, sin cookies de sesion
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.GET, "/internal/status").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/accounts/{id}").hasAuthority("ROLE_ACCOUNT_READ")
                .pathMatchers(HttpMethod.POST, "/api/accounts").hasAuthority("ROLE_ACCOUNT_WRITE")
                .anyExchange().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
            ))
            .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}

