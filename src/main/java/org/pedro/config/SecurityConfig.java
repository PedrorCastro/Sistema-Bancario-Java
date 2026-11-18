package org.pedro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.pedro.security.JwtRoleConverter;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
        JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
                )
              
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()

                // Cliente autenticado pode consultar os próprios dados
                .requestMatchers(HttpMethod.GET, "/clientes/me").hasRole("CLIENTE")

                // Operações administrativas
                .requestMatchers(HttpMethod.POST, "/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("ADMIN")

                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        JwtRoleConverter roleConverter = new JwtRoleConverter();

        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(roleConverter);

        return jwtAuthConverter;

    }


}
