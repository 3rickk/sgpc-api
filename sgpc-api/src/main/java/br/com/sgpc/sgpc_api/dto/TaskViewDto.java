package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskViewDto {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private String statusDescription;
    private LocalDate startDatePlanned;
    private LocalDate endDatePlanned;
    private LocalDate startDateActual;
    private LocalDate endDateActual;
    private Integer progressPercentage;
    private Integer priority;
    private String priorityDescription;
    private Integer estimatedHours;
    private Integer actualHours;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dados do projeto
    private Long projectId;
    private String projectName;

    // Dados do usuário responsável
    private Long assignedUserId;
    private String assignedUserName;
    private String assignedUserEmail;

    // Dados do usuário que criou
    private Long createdByUserId;
    private String createdByUserName;
    private String createdByUserEmail;

    // Flags de conveniência
    private boolean isAssigned;
    private boolean isCompleted;
    private boolean isInProgress;
    private boolean isOverdue;
} 