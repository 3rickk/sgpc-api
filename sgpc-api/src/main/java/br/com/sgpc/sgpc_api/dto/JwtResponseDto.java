package br.com.sgpc.sgpc_api.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de autenticação JWT.
 * 
 * Representa a resposta retornada após uma autenticação bem-sucedida,
 * contendo o token JWT e informações básicas do usuário autenticado.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação contendo token JWT e dados do usuário")
public class JwtResponseDto {
    
    /**
     * Token JWT para autenticação.
     * 
     * Token válido que deve ser incluído no header Authorization
     * das requisições subsequentes.
     */
    @Schema(description = "Token JWT para autenticação", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    /**
     * Tipo do token de autenticação.
     * 
     * Sempre será "Bearer" para tokens JWT.
     */
    @Schema(description = "Tipo do token", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";
    
    /**
     * ID único do usuário autenticado.
     */
    @Schema(description = "ID único do usuário", example = "1")
    private Long userId;
    
    /**
     * Email do usuário autenticado.
     */
    @Schema(description = "Email do usuário", example = "usuario@sgpc.com")
    private String email;
    
    /**
     * Nome completo do usuário autenticado.
     */
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String fullName;
    
    /**
     * Conjunto de roles/permissões do usuário.
     * 
     * Define as permissões e níveis de acesso do usuário no sistema.
     */
    @Schema(description = "Roles/permissões do usuário", example = "[\"USER\", \"PROJECT_MANAGER\"]")
    private Set<String> roles;
    
    /**
     * Construtor para criação da resposta JWT.
     * 
     * @param token Token JWT gerado
     * @param userId ID do usuário
     * @param email Email do usuário  
     * @param fullName Nome completo do usuário
     * @param roles Conjunto de roles do usuário
     */
    public JwtResponseDto(String token, Long userId, String email, String fullName, Set<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
} 