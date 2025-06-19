package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de usuários por administradores.
 * 
 * Permite que administradores criem usuários com diferentes roles
 * (USER ou MANAGER). Usado apenas por endpoints administrativos.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de usuário por administrador")
public class UserCreateDto {
    
    /**
     * Nome completo do usuário.
     */
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos", required = true)
    private String fullName;
    
    /**
     * Email do usuário para login e comunicação.
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Schema(description = "Email do usuário", example = "joao.silva@sgpc.com", required = true)
    private String email;
    
    /**
     * Telefone do usuário para contato.
     */
    @Schema(description = "Telefone do usuário", example = "(11) 99999-9999")
    private String phone;
    
    /**
     * Senha para acesso ao sistema.
     */
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Schema(description = "Senha do usuário", example = "senha123", required = true)
    private String password;
    
    /**
     * Taxa horária do usuário para cálculos de custo.
     */
    @Schema(description = "Taxa horária do usuário para cálculos de custo", example = "50.00")
    private BigDecimal hourlyRate;
    
    /**
     * Role do usuário.
     * Apenas USER ou MANAGER são permitidos para criação por admin.
     */
    @NotBlank(message = "Role é obrigatória")
    @Pattern(regexp = "^(USER|MANAGER)$", message = "Role deve ser USER ou MANAGER")
    @Schema(description = "Role do usuário", example = "USER", allowableValues = {"USER", "MANAGER"}, required = true)
    private String roleName;
} 