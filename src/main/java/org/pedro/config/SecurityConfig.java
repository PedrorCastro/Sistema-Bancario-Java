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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Autenticação
                .requestMatchers("/auth/login").permitAll()

                // Cadastro público de clientes
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()

                // Consulta dos próprios dados
                .requestMatchers(HttpMethod.GET, "/clientes/me")
                    .hasRole("CLIENTE")

                // Administração de clientes
                .requestMatchers("/clientes/**").hasRole("ADMIN")

                // Criação de contas
                .requestMatchers(HttpMethod.POST, "/contas")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Listagem das próprias contas
                .requestMatchers(HttpMethod.GET, "/contas/minhas")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Listagem de todas as contas
                .requestMatchers(HttpMethod.GET, "/contas")
                    .hasRole("ADMIN")

                // Consulta individual
                .requestMatchers(HttpMethod.GET, "/contas/{id}")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Exclusão de conta
                .requestMatchers(HttpMethod.DELETE, "/contas/{id}")
                    .hasAnyRole("CLIENTE", "ADMIN")

                .requestMatchers("/error").permitAll()

                // Documentação da API
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Todo o restante exige autenticação
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
