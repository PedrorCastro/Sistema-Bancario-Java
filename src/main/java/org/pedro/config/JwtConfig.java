package org.pedro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Value;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Configuration
public class JwtConfig{

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    

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

}