package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de usuários do sistema.
 * 
 * Esta classe representa um usuário do sistema SGPC,
 * incluindo informações pessoais, profissionais e de acesso.
 * Utilizada para transferência de dados sem expor informações sensíveis.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de um usuário do sistema")
public class UserDto {
    
    @Schema(description = "ID único do usuário", example = "1")
    private Long id;
    
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos")
    private String fullName;
    
    @Schema(description = "Email do usuário (login)", example = "joao.silva@empresa.com")
    private String email;
    
    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String phone;
    
    @Schema(description = "Valor da hora trabalhada", example = "75.50")
    private BigDecimal hourlyRate;
    
    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Data de criação da conta", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Funções do usuário no sistema", example = "[\"USER\", \"PROJECT_MANAGER\"]")
    private Set<String> roles;
} 