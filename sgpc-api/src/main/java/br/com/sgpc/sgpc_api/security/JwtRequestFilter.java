package br.com.sgpc.sgpc_api.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;

/**
 * Filtro de interceptação de requisições para validação de tokens JWT.
 * 
 * Este filtro é executado uma vez por requisição HTTP e tem a responsabilidade
 * de interceptar todas as requisições, extrair o token JWT do header Authorization,
 * validar sua integridade e autenticidade, e configurar o contexto de segurança
 * do Spring Security quando o token é válido.
 * 
 * Funcionalidades principais:
 * - Interceptação de todas as requisições HTTP
 * - Extração do token JWT do header "Authorization"
 * - Validação do token com JwtUtil
 * - Carregamento dos detalhes do usuário
 * - Configuração do contexto de segurança
 * - Log detalhado de operações de segurança
 * 
 * Formato esperado do header:
 * Authorization: Bearer {token}
 * 
 * Características especiais:
 * - Extends OncePerRequestFilter para garantir execução única
 * - Tratamento de exceções com logs detalhados
 * - Validação completa de token e usuário
 * - Integração completa com Spring Security
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Determina se este filtro deve ser pulado para certas URLs.
     * 
     * Endpoints públicos como autenticação, swagger e health check
     * não precisam de validação JWT.
     * 
     * @param request requisição HTTP
     * @return boolean true se o filtro deve ser pulado
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Lista de caminhos que não precisam de autenticação JWT
        return path.startsWith("/api/auth/") || 
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/api-docs/") ||
               path.startsWith("/v3/api-docs/") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/actuator/health") ||
               path.equals("/error");
    }
    
    /**
     * Método principal de filtro executado para cada requisição HTTP.
     * 
     * Este método implementa o fluxo completo de autenticação JWT:
     * 1. Extrai o token do header Authorization
     * 2. Valida o formato "Bearer {token}"
     * 3. Extrai o username do token
     * 4. Carrega os detalhes do usuário
     * 5. Valida o token contra os detalhes do usuário
     * 6. Configura o contexto de segurança (se válido)
     * 7. Continua a cadeia de filtros
     * 
     * @param request requisição HTTP recebida
     * @param response resposta HTTP a ser enviada
     * @param filterChain cadeia de filtros do Spring Security
     * @throws ServletException se erro de servlet
     * @throws IOException se erro de I/O
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // Extrai o token JWT do header Authorization
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            // Verifica se o token não está vazio após remover "Bearer "
            if (jwtToken.trim().isEmpty()) {
                logger.warn("Token JWT vazio encontrado no header Authorization");
            } else {
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                } catch (Exception e) {
                    logger.error("Não foi possível extrair o username do token JWT: {}", e.getMessage());
                }
            }
        } else if (requestTokenHeader != null) {
            logger.warn("Token JWT não começa com Bearer String");
        }
        
        // Valida o token e configura o contexto de segurança
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetailsImpl userDetails = (UserDetailsImpl) this.userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    // Token válido - configura o contexto de segurança
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Token JWT válido para usuário: {}", username);
                } else {
                    logger.warn("Token JWT inválido para usuário: {}", username);
                }
            } catch (Exception e) {
                logger.error("Erro ao validar token JWT para usuário {}: {}", username, e.getMessage());
            }
        }
        
        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
} 