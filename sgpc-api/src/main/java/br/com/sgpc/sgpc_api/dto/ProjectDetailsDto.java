package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailsDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDatePlanned;
    private LocalDate endDatePlanned;
    private LocalDate startDateActual;
    private LocalDate endDateActual;
    private BigDecimal totalBudget;
    private String client;
    private ProjectStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UserDto> teamMembers;
    private int teamSize;
    
    // Campos calculados
    private Integer progressPercentage;
    private BigDecimal budgetUsed;
    private BigDecimal budgetRemaining;
    private Boolean isDelayed;
    private Long daysRemaining;
} 