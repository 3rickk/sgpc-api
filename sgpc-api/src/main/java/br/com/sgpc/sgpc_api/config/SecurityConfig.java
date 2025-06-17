package br.com.sgpc.sgpc_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Configuração de segurança da aplicação SGPC.
 * 
 * Esta classe centraliza as configurações relacionadas à segurança
 * da aplicação, incluindo codificação de senhas e outras configurações
 * de segurança necessárias para o funcionamento do sistema.
 * 
 * Configuração para API REST:
 * - Desabilita form login (tela de login HTML)
 * - Desabilita autenticação HTTP Basic
 * - Configura para JWT stateless
 * - Permite acesso livre aos endpoints de auth e documentação
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * Bean de codificação de senhas.
     * 
     * IMPORTANTE: Esta é uma implementação temporária e simplificada.
     * Em ambiente de produção, deve retornar uma instância real de
     * PasswordEncoder como BCryptPasswordEncoder.
     * 
     * Implementação recomendada para produção:
     * {@code return new BCryptPasswordEncoder(12);}
     * 
     * @return String placeholder para codificador de senhas
     * @deprecated Implementação temporária - substituir por PasswordEncoder real
     */
    @Bean
    public String passwordEncoder() {
        // Simplificado - retorna uma string dummy
        // TODO: Substituir por new BCryptPasswordEncoder(12) em produção
        return "bcrypt-encoder";
    }
    
    /**
     * Configura o filtro de segurança para API REST.
     * 
     * Remove a tela de login padrão e configura a aplicação
     * para funcionar como uma API pura com autenticação JWT.
     * 
     * @param http configuração de segurança HTTP
     * @return SecurityFilterChain configurado para API
     * @throws Exception se erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configura CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configura autorização de requests
            .authorizeHttpRequests(auth -> auth
                // Permite acesso livre aos endpoints de autenticação
                .requestMatchers("/api/auth/**").permitAll()
                
                // Permite acesso livre à documentação Swagger
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                
                // Permite acesso ao endpoint de health check
                .requestMatchers("/actuator/health").permitAll()
                
                // Outras requisições não precisam de autenticação por enquanto
                .anyRequest().permitAll()
            )
            
            // Desabilita form login (remove a tela de login HTML)
            .formLogin(form -> form.disable())
            
            // Desabilita HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Configura sessão como stateless (para APIs REST com JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
    
    /**
     * Configura CORS para permitir requisições de diferentes origens.
     * 
     * @return CorsConfigurationSource configuração de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite todas as origens (para desenvolvimento)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Permite todos os métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Permite todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permite credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 