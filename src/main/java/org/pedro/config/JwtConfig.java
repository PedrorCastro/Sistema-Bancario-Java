package org.pedro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Value;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;


@Configuration
public class JwtConfig{

    @Value("${jwt.secret}")
    private String secretKey;
    
    @Bean
    public SecretKey secretKey(){
        if (secretKey == null || secretKey.isBlank()){
            throw new IllegalArgumentException("A chave nao pode ser nula!"); 
        }
        
        try {
           
            byte[] chaveEmBytes = Base64.getDecoder().decode(secretKey);

            
            if (chaveEmBytes.length < 32) {
                throw new IllegalArgumentException("Tamanho de chave inválido. A chave deve ter pelo menos 32 bytes.");
            }
            return new SecretKeySpec(chaveEmBytes, "HmacSHA256");

        } catch (IllegalArgumentException e) {
            // Se quiser capturar centralizado ou tratar o erro de inicialização
            throw new RuntimeException("Falha na inicialização: chave inválida.", e);
        }    
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey){
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretKey).keyID("banco-api").build();

        JWKSet jwkSet = new JWKSet(jwk);

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey){

        return NimbusJwtDecoder.withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build(); 
    }
}