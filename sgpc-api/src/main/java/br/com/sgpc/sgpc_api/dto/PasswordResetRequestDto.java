package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitação de reset de senha.
 * 
 * Este DTO é utilizado quando um usuário solicita o reset de sua senha,
 * fornecendo apenas o email para identificação.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para solicitação de reset de senha")
public class PasswordResetRequestDto {
    
    /**
     * Email do usuário que solicita o reset de senha.
     * Deve ser um email válido e existente no sistema.
     */
    @Schema(
        description = "Email do usuário para reset de senha",
        example = "usuario@exemplo.com",
        required = true,
        maxLength = 100
    )
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;
} 