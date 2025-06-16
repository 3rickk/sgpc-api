package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro de novos usuários.
 * 
 * Representa os dados necessários para criar uma nova conta de usuário
 * no sistema, incluindo informações pessoais, credenciais e configurações.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para registro de um novo usuário")
public class UserRegistrationDto {
    
    /**
     * Nome completo do usuário.
     * 
     * Deve conter pelo menos 2 caracteres e no máximo 255.
     */
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos", required = true)
    private String fullName;
    
    /**
     * Email do usuário para login e comunicação.
     * 
     * Deve ser um endereço de email válido e único no sistema.
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Schema(description = "Email do usuário", example = "joao.silva@sgpc.com", required = true)
    private String email;
    
    /**
     * Telefone do usuário para contato.
     * 
     * Campo opcional para contato telefônico.
     */
    @Schema(description = "Telefone do usuário", example = "(11) 99999-9999")
    private String phone;
    
    /**
     * Senha para acesso ao sistema.
     * 
     * Deve ter pelo menos 6 caracteres para segurança básica.
     */
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Schema(description = "Senha do usuário", example = "minhasenha123", required = true)
    private String password;
    
    /**
     * Taxa horária do usuário para cálculos de custo.
     * 
     * Valor opcional usado para calcular custos de mão de obra
     * em projetos e tarefas.
     */
    @Schema(description = "Taxa horária do usuário para cálculos de custo", example = "50.00")
    private BigDecimal hourlyRate;
    
    /**
     * Conjunto de roles/permissões do usuário.
     * 
     * Define as permissões e níveis de acesso que o usuário
     * terá no sistema após o registro.
     */
    @Schema(description = "Roles/permissões do usuário", example = "[\"USER\", \"MANAGER\"]")
    private Set<String> roleNames;
} 