package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para redefinição de senha do usuário.
 * 
 * Esta classe contém os dados necessários para permitir que um usuário
 * redefina sua senha usando um token de recuperação válido enviado
 * por email.
 * 
 * Funcionalidades:
 * - Validação de token de recuperação
 * - Definição de nova senha com validação mínima
 * - Processo seguro de reset de senha
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para redefinição de senha")
public class PasswordResetDto {
    
    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token de recuperação de senha enviado por email", 
            example = "abc123def456ghi789",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
    
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Schema(description = "Nova senha do usuário", 
            example = "novaSenha123",
            minLength = 6,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
} 