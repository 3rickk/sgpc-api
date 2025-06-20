package br.com.sgpc.sgpc_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import br.com.sgpc.sgpc_api.security.JwtRequestFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuração de segurança da aplicação SGPC.
 * 
 * Esta classe centraliza as configurações relacionadas à segurança
 * da aplicação, incluindo codificação de senhas, filtros JWT e
 * configurações de autorização para proteger os endpoints da API.
 * 
 * Configuração para API REST:
 * - Desabilita form login (tela de login HTML)
 * - Desabilita autenticação HTTP Basic
 * - Configura para JWT stateless
 * - Permite acesso livre aos endpoints de auth e documentação
 * - Protege todos os demais endpoints com JWT
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    /**
     * Bean de codificação de senhas usando BCrypt.
     * 
     * @return PasswordEncoder configurado com BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * Configura o filtro de segurança para API REST.
     * 
     * Remove a tela de login padrão e configura a aplicação
     * para funcionar como uma API pura com autenticação JWT.
     * Protege todos os endpoints exceto os de autenticação,
     * documentação e health check.
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
                
                // Permite acesso livre ao endpoint de ping (health check)
                .requestMatchers("/ping").permitAll()
                
                // Permite acesso livre à documentação Swagger
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                
                // Permite acesso ao endpoint de health check
                .requestMatchers("/actuator/health").permitAll()
                
                // Permite acesso aos recursos estáticos
                .requestMatchers("/error").permitAll()
                
                // TODOS OS DEMAIS ENDPOINTS REQUEREM AUTENTICAÇÃO
                .anyRequest().authenticated()
            )
            
            // Desabilita form login (remove a tela de login HTML)
            .formLogin(form -> form.disable())
            
            // Desabilita HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Configura sessão como stateless (para APIs REST com JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configura o ponto de entrada para requisições não autenticadas e acesso negado
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            
            // Adiciona o filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
            
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

    /**
     * Configura o ponto de entrada para requisições não autenticadas.
     * 
     * @return AuthenticationEntryPoint customizado
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, 
                org.springframework.security.core.AuthenticationException authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            Map<String, Object> body = new HashMap<>();
            body.put("status", 401);
            body.put("error", "Não autorizado");
            body.put("message", "Token JWT requerido para acessar este endpoint");
            body.put("path", request.getRequestURI());
            body.put("timestamp", new Date());
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
        };
    }

    /**
     * Configura o tratamento para acesso negado (403 Forbidden).
     * 
     * @return AccessDeniedHandler customizado
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            
            Map<String, Object> body = new HashMap<>();
            body.put("status", 403);
            body.put("error", "Acesso negado");
            body.put("message", "Você não tem permissão para acessar este recurso. Verifique seu nível de acesso.");
            body.put("path", request.getRequestURI());
            body.put("timestamp", new Date());
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
        };
    }
} 