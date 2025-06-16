package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de tarefas.
 * 
 * Contém todos os dados necessários para criar uma nova tarefa
 * dentro de um projeto, incluindo prazos, prioridade e usuário responsável.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para criação de uma nova tarefa")
public class TaskCreateDto {

    /**
     * Título da tarefa.
     * 
     * Nome descritivo e conciso da tarefa a ser executada.
     */
    @NotBlank(message = "Título da tarefa é obrigatório")
    @Size(min = 3, max = 255, message = "Título deve ter entre 3 e 255 caracteres")
    @Schema(description = "Título da tarefa", example = "Instalação do sistema elétrico", required = true)
    private String title;

    /**
     * Descrição detalhada da tarefa.
     * 
     * Explicação detalhada do que deve ser feito na tarefa.
     */
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada da tarefa", 
            example = "Instalar toda a rede elétrica do primeiro andar, incluindo tomadas e pontos de luz")
    private String description;

    /**
     * Status inicial da tarefa.
     * 
     * Se não especificado, será definido como A_FAZER por padrão.
     */
    @Schema(description = "Status da tarefa", 
            example = "A_FAZER", 
            allowableValues = {"A_FAZER", "EM_ANDAMENTO", "CONCLUIDA", "BLOQUEADA", "CANCELADA"},
            defaultValue = "A_FAZER")
    private TaskStatus status = TaskStatus.A_FAZER;

    /**
     * Data planejada para início da tarefa.
     */
    @Schema(description = "Data planejada para início da tarefa", example = "2024-02-01")
    private LocalDate startDatePlanned;

    /**
     * Data planejada para conclusão da tarefa.
     */
    @Schema(description = "Data planejada para conclusão da tarefa", example = "2024-02-10")
    private LocalDate endDatePlanned;

    /**
     * Data real de início da tarefa.
     * 
     * Preenchida quando a tarefa é efetivamente iniciada.
     */
    @Schema(description = "Data real de início da tarefa", example = "2024-02-02")
    private LocalDate startDateActual;

    /**
     * Data real de conclusão da tarefa.
     * 
     * Preenchida quando a tarefa é concluída.
     */
    @Schema(description = "Data real de conclusão da tarefa", example = "2024-02-12")
    private LocalDate endDateActual;

    /**
     * Percentual de progresso da tarefa.
     * 
     * Valor entre 0 e 100 indicando o progresso atual da tarefa.
     */
    @Min(value = 0, message = "Progresso deve ser entre 0 e 100")
    @Max(value = 100, message = "Progresso deve ser entre 0 e 100")
    @Schema(description = "Percentual de progresso (0-100)", example = "0", defaultValue = "0")
    private Integer progressPercentage = 0;

    /**
     * Prioridade da tarefa.
     * 
     * Escala de 1 a 4, onde:
     * 1 = Baixa, 2 = Normal, 3 = Alta, 4 = Crítica
     */
    @Min(value = 1, message = "Prioridade deve ser entre 1 e 4")
    @Max(value = 4, message = "Prioridade deve ser entre 1 e 4")
    @NotNull(message = "Prioridade é obrigatória")
    @Schema(description = "Prioridade da tarefa (1=Baixa, 2=Normal, 3=Alta, 4=Crítica)", 
            example = "2", 
            required = true,
            defaultValue = "1")
    private Integer priority = 1;

    /**
     * Horas estimadas para conclusão da tarefa.
     * 
     * Estimativa inicial do tempo necessário para completar a tarefa.
     */
    @Min(value = 0, message = "Horas estimadas deve ser positivo")
    @Schema(description = "Horas estimadas para conclusão", example = "40")
    private Integer estimatedHours;

    /**
     * Horas reais gastas na tarefa.
     * 
     * Tempo efetivamente gasto na execução da tarefa.
     */
    @Min(value = 0, message = "Horas reais deve ser positivo")
    @Schema(description = "Horas reais gastas na tarefa", example = "45")
    private Integer actualHours;

    /**
     * Notas adicionais sobre a tarefa.
     * 
     * Observações, comentários ou informações extras sobre a tarefa.
     */
    @Size(max = 3000, message = "Notas devem ter no máximo 3000 caracteres")
    @Schema(description = "Notas adicionais sobre a tarefa", 
            example = "Aguardar liberação do fornecedor para os materiais")
    private String notes;

    /**
     * ID do usuário responsável pela tarefa.
     * 
     * Usuário que será responsável pela execução da tarefa.
     */
    @Schema(description = "ID do usuário responsável pela tarefa", example = "1")
    private Long assignedUserId;
} 