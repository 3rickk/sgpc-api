package br.com.sgpc.sgpc_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
    

    
    @Bean
    public String passwordEncoder() {
        // Simplificado - retorna uma string dummy
        return "bcrypt-encoder";
    }
} 