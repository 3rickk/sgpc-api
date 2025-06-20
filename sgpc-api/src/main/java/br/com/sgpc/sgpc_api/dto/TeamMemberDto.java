package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para membros da equipe nos relatórios de projetos.
 * 
 * Contém informações sobre os usuários que fazem parte
 * da equipe de um projeto específico.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Membro da equipe do projeto")
public class TeamMemberDto {

    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo", example = "João Silva Santos")
    private String fullName;

    @Schema(description = "Email", example = "joao.silva@empresa.com")
    private String email;

    @Schema(description = "Cargo/Função", example = "Engenheiro Civil")
    private String role;

    @Schema(description = "Número de tarefas atribuídas", example = "5")
    private Integer assignedTasksCount;

    @Schema(description = "Número de tarefas concluídas", example = "3")
    private Integer completedTasksCount;

    @Schema(description = "Percentual de conclusão das tarefas", example = "60.0")
    private Double taskCompletionRate;

    @Schema(description = "Status do usuário", example = "ATIVO")
    private String status;
} 