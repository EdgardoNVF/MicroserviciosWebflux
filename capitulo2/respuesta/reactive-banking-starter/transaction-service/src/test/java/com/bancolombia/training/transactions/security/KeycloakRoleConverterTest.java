package com.bancolombia.training.transactions.security;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRoleConverterTest {
    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test void convierteRolesDeRealm() {
        Jwt jwt = Jwt.withTokenValue("token-para-test")
            .header("alg", "RS256")
            .claim("sub", UUID.randomUUID().toString())
            .claim("realm_access", Map.of("roles", List.of("ACCOUNT_READ", "TRANSACTION_WRITE")))
            .build();
        var roles = converter.convert(jwt).stream().map(a -> a.getAuthority()).toList();
        assertTrue(roles.contains("ROLE_ACCOUNT_READ"));
        assertTrue(roles.contains("ROLE_TRANSACTION_WRITE"));
    }

    @Test void sinRolesNoOtorgaPermisos() {
        Jwt jwt = Jwt.withTokenValue("token-para-test").header("alg", "RS256")
            .claim("sub", "usuario").build();
        assertTrue(converter.convert(jwt).isEmpty());
    }
}
