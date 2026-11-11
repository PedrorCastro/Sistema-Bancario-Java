package org.pedro.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>>{
    
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String role = jwt.getClaimAsString("role");

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return List.of(authority);

    }
}