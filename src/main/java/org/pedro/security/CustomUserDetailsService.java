package org.pedro.security;

import org.pedro.model.Cliente;
import org.pedro.repository.ClienteRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String emailNormalizado = email.trim().toLowerCase();

        Cliente cliente = clienteRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cliente não encontrado: " + emailNormalizado
                ));

        return new ClienteDetails(cliente);
    }
}