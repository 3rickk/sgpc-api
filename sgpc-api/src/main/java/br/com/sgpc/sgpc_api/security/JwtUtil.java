package br.com.sgpc.sgpc_api.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utilitário para geração e validação de tokens JWT.
 * 
 * Esta classe centraliza todas as operações relacionadas a tokens JWT
 * no sistema SGPC, incluindo geração, validação, extração de dados e
 * verificação de expiração. Utiliza a biblioteca JJWT para operações
 * criptográficas seguras.
 * 
 * Funcionalidades principais:
 * - Geração de tokens JWT para autenticação
 * - Validação de tokens recebidos
 * - Extração de informações do token (username, expiração)
 * - Verificação de expiração
 * - Assinatura e verificação segura com HMAC-SHA
 * 
 * Configurações via properties:
 * - sgpc.jwt.secret: chave secreta para assinatura
 * - sgpc.jwt.expiration: tempo de expiração em milissegundos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${sgpc.jwt.secret}")
    private String jwtSecret;
    
    @Value("${sgpc.jwt.expiration}")
    private int jwtExpirationMs;
    
    /**
     * Gera a chave de assinatura a partir do secret configurado.
     * 
     * Decodifica o secret em Base64 e cria uma SecretKey para
     * assinatura HMAC-SHA dos tokens JWT.
     * 
     * @return SecretKey chave para assinatura JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Gera um token JWT para o usuário autenticado.
     * 
     * Cria um token com o username como subject e configura
     * o tempo de expiração baseado na configuração do sistema.
     * 
     * @param userDetails detalhes do usuário autenticado
     * @return String token JWT assinado
     */
    public String generateToken(UserDetailsImpl userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    /**
     * Cria o token JWT com claims personalizadas.
     * 
     * Método interno que constrói o token com todas as informações
     * necessárias: claims, subject, datas de emissão e expiração.
     * 
     * @param claims informações adicionais do token
     * @param subject subject do token (username)
     * @return String token JWT completo
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extrai o username (subject) do token JWT.
     * 
     * @param token token JWT a ser processado
     * @return String username do usuário
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrai a data de expiração do token JWT.
     * 
     * @param token token JWT a ser processado
     * @return Date data de expiração do token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrai uma claim específica do token usando função personalizada.
     * 
     * Método genérico que permite extrair qualquer informação das
     * claims do token através de uma função de resolução.
     * 
     * @param <T> tipo da claim a ser extraída
     * @param token token JWT a ser processado
     * @param claimsResolver função que resolve a claim desejada
     * @return T valor da claim extraída
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrai todas as claims do token JWT.
     * 
     * Decodifica e valida o token, retornando todas as claims
     * contidas nele. Método interno usado por outros métodos
     * de extração.
     * 
     * @param token token JWT a ser processado
     * @return Claims todas as claims do token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Verifica se o token JWT está expirado.
     * 
     * Compara a data de expiração do token com a data atual
     * para determinar se o token ainda é válido.
     * 
     * @param token token JWT a ser verificado
     * @return Boolean true se o token está expirado
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Valida o token JWT para um usuário específico.
     * 
     * Verifica se o token é válido, se o username corresponde
     * ao usuário e se o token não está expirado.
     * 
     * @param token token JWT a ser validado
     * @param userDetails detalhes do usuário para comparação
     * @return Boolean true se o token é válido para o usuário
     */
    public Boolean validateToken(String token, UserDetailsImpl userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida o token JWT apenas verificando integridade e expiração.
     * 
     * Versão simplificada da validação que apenas verifica se
     * o token é bem formado, assinado corretamente e não expirado.
     * 
     * @param token token JWT a ser validado
     * @return Boolean true se o token é válido
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }
} 