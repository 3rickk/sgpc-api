package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login.
 * 
 * Representa os dados necessários para autenticação de um usuário
 * no sistema, contendo email e senha.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para login no sistema")
public class LoginRequestDto {
    
    /**
     * Email do usuário para autenticação.
     * 
     * Deve ser um endereço de email válido e não pode estar vazio.
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Schema(description = "Email do usuário", example = "usuario@sgpc.com", required = true)
    private String email;
    
    /**
     * Senha do usuário para autenticação.
     * 
     * Não pode estar vazia ou conter apenas espaços em branco.
     */
    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "minhasenha123", required = true)
    private String password;
} 