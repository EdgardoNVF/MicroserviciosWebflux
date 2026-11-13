package com.bancolombia.training.accounts.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Convierte los roles de realm_access.roles de Keycloak a ROLE_*. */
public final class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) return List.of();
        Object roles = realmAccess.get("roles");
        if (!(roles instanceof Collection<?> roleValues)) return List.of();

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Object role : roleValues) {
            if (role instanceof String value && value.matches("[A-Za-z0-9_:-]{1,80}")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + value));
            }
        }
        return authorities;
    }
}

