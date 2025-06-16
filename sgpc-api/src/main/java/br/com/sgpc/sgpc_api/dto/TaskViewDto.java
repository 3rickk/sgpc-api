package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para visualização completa de tarefas.
 * 
 * Este DTO contém todas as informações de uma tarefa, incluindo dados
 * do projeto associado, usuário responsável, criador e flags de conveniência
 * para facilitar a exibição na interface.
 * 
 * @author SGPC API
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de visualização de tarefa")
public class TaskViewDto {

    /**
     * ID único da tarefa.
     */
    @Schema(description = "ID único da tarefa", example = "1")
    private Long id;
    
    /**
     * Título da tarefa.
     */
    @Schema(description = "Título da tarefa", example = "Instalação de sistema elétrico")
    private String title;
    
    /**
     * Descrição detalhada da tarefa.
     */
    @Schema(description = "Descrição detalhada da tarefa", example = "Instalação completa do sistema elétrico incluindo pontos de luz e tomadas")
    private String description;
    
    /**
     * Status atual da tarefa.
     */
    @Schema(description = "Status atual da tarefa", example = "IN_PROGRESS")
    private TaskStatus status;
    
    /**
     * Descrição textual do status.
     */
    @Schema(description = "Descrição textual do status", example = "Em Progresso")
    private String statusDescription;
    
    /**
     * Data planejada para início da tarefa.
     */
    @Schema(description = "Data planejada para início", example = "2024-01-15")
    private LocalDate startDatePlanned;
    
    /**
     * Data planejada para conclusão da tarefa.
     */
    @Schema(description = "Data planejada para conclusão", example = "2024-01-25")
    private LocalDate endDatePlanned;
    
    /**
     * Data real de início da tarefa.
     */
    @Schema(description = "Data real de início", example = "2024-01-16")
    private LocalDate startDateActual;
    
    /**
     * Data real de conclusão da tarefa.
     */
    @Schema(description = "Data real de conclusão", example = "2024-01-24")
    private LocalDate endDateActual;
    
    /**
     * Percentual de progresso da tarefa (0-100%).
     */
    @Schema(description = "Percentual de progresso", example = "75", minimum = "0", maximum = "100")
    private Integer progressPercentage;
    
    /**
     * Prioridade da tarefa (1-5, sendo 5 a mais alta).
     */
    @Schema(description = "Prioridade da tarefa", example = "3", minimum = "1", maximum = "5")
    private Integer priority;
    
    /**
     * Descrição textual da prioridade.
     */
    @Schema(description = "Descrição da prioridade", example = "Média")
    private String priorityDescription;
    
    /**
     * Horas estimadas para conclusão.
     */
    @Schema(description = "Horas estimadas", example = "40")
    private Integer estimatedHours;
    
    /**
     * Horas realmente trabalhadas.
     */
    @Schema(description = "Horas reais trabalhadas", example = "32")
    private Integer actualHours;
    
    /**
     * Notas e observações da tarefa.
     */
    @Schema(description = "Notas e observações", example = "Tarefa progredindo conforme planejado")
    private String notes;
    
    /**
     * Data de criação da tarefa.
     */
    @Schema(description = "Data de criação", example = "2024-01-10T10:30:00")
    private LocalDateTime createdAt;
    
    /**
     * Data da última atualização.
     */
    @Schema(description = "Data da última atualização", example = "2024-01-20T14:15:00")
    private LocalDateTime updatedAt;

    /**
     * ID do projeto associado.
     */
    @Schema(description = "ID do projeto", example = "1")
    private Long projectId;
    
    /**
     * Nome do projeto associado.
     */
    @Schema(description = "Nome do projeto", example = "Construção Residencial XYZ")
    private String projectName;

    /**
     * ID do usuário responsável pela tarefa.
     */
    @Schema(description = "ID do usuário responsável", example = "5")
    private Long assignedUserId;
    
    /**
     * Nome do usuário responsável.
     */
    @Schema(description = "Nome do usuário responsável", example = "João Silva")
    private String assignedUserName;
    
    /**
     * Email do usuário responsável.
     */
    @Schema(description = "Email do usuário responsável", example = "joao.silva@empresa.com")
    private String assignedUserEmail;

    /**
     * ID do usuário que criou a tarefa.
     */
    @Schema(description = "ID do usuário criador", example = "2")
    private Long createdByUserId;
    
    /**
     * Nome do usuário que criou a tarefa.
     */
    @Schema(description = "Nome do usuário criador", example = "Maria Santos")
    private String createdByUserName;
    
    /**
     * Email do usuário que criou a tarefa.
     */
    @Schema(description = "Email do usuário criador", example = "maria.santos@empresa.com")
    private String createdByUserEmail;

    /**
     * Indica se a tarefa tem usuário responsável atribuído.
     */
    @Schema(description = "Indica se tem usuário atribuído", example = "true")
    private boolean isAssigned;
    
    /**
     * Indica se a tarefa está concluída.
     */
    @Schema(description = "Indica se está concluída", example = "false")
    private boolean isCompleted;
    
    /**
     * Indica se a tarefa está em progresso.
     */
    @Schema(description = "Indica se está em progresso", example = "true")
    private boolean isInProgress;
    
    /**
     * Indica se a tarefa está atrasada.
     */
    @Schema(description = "Indica se está atrasada", example = "false")
    private boolean isOverdue;
} 