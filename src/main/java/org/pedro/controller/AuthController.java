package org.pedro.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.pedro.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.pedro.dto.request.AuthRequestDTO;
import org.pedro.dto.response.AuthResponseDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.pedro.security.ClienteDetails;

@RestController
@RequestMapping("/auth")
public class AuthController{

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    


    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.senha()
            )
        );
        ClienteDetails usuario = (ClienteDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(usuario);

        return new AuthResponseDTO(
            token,
            "Bearer",
            jwtService.getExpiration()
        );
    }
}