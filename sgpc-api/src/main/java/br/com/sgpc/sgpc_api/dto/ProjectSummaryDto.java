package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDto {

    private Long id;
    private String name;
    private String client;
    private ProjectStatus status;
    private String statusDescription;
    private LocalDate startDatePlanned;
    private LocalDate endDatePlanned;
    private BigDecimal totalBudget;
    private LocalDateTime createdAt;
    private int teamSize;
    private Integer progressPercentage;
    private Boolean isDelayed;
} 