package br.com.sgpc.sgpc_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.sgpc.sgpc_api.security.JwtRequestFilter;
import br.com.sgpc.sgpc_api.security.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Bean
    public String passwordEncoder() {
        // Simplificado - retorna uma string dummy
        return "bcrypt-encoder";
    }
} 