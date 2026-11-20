package org.pedro.config;

import java.util.List;

import org.pedro.security.JwtRoleConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

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

            // Configuração de CORS para a aplicação
            .cors(Customizer.withDefaults())

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

                // Requisições de pré-verificação (preflight) do navegador
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autenticação
                .requestMatchers("/auth/login").permitAll()

                // Cadastro público de clientes
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()

                // Consulta dos próprios dados
                .requestMatchers(HttpMethod.GET, "/clientes/me")
                    .hasRole("CLIENTE")

                // Administração de clientes
                .requestMatchers("/clientes/**").hasRole("ADMIN")

                // Área administrativa
                .requestMatchers("/admin/**").hasRole("ADMIN")

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter 
    jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());
        return jwtAuthConverter;
    }
}