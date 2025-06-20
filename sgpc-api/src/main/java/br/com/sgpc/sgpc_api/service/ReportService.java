package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.CostReportDto;
import br.com.sgpc.sgpc_api.dto.ProjectReportDto;
import br.com.sgpc.sgpc_api.dto.StockReportDto;
import br.com.sgpc.sgpc_api.dto.TaskSummaryDto;
import br.com.sgpc.sgpc_api.dto.TeamMemberDto;
import br.com.sgpc.sgpc_api.entity.Material;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;

/**
 * Service responsável pela geração de relatórios do sistema.
 * 
 * Este service oferece diferentes tipos de relatórios:
 * - Relatório de Projetos: progresso, datas, orçamento
 * - Relatório de Custos: análise financeira por projeto
 * - Relatório de Estoque: situação atual dos materiais
 * 
 * Todos os cálculos incluem métricas como:
 * - Percentuais de progresso e utilização
 * - Indicadores de atraso e baixo estoque
 * - Comparações entre planejado vs realizado
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MaterialRepository materialRepository;

    /**
     * Gera relatório de um projeto específico pelo ID.
     * 
     * @param projectId ID do projeto
     * @return Lista com dados do projeto específico ou lista vazia se não encontrado
     */
    public List<ProjectReportDto> getProjectReportById(Long projectId) {
        return projectRepository.findById(projectId)
            .map(project -> {
                List<Task> projectTasks = taskRepository.findByProjectId(project.getId());
                Integer totalTasks = projectTasks.size();
                Integer completedTasks = (int) projectTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.CONCLUIDA)
                    .count();
                
                Integer progressPercentage = calculateProgressPercentage(project, totalTasks, completedTasks);
                Boolean delayed = isProjectDelayed(project);
                Long daysRemaining = calculateDaysRemaining(project);
                
                return List.of(buildDetailedProjectReport(project));
            })
            .orElse(List.of());
    }

    /**
     * Gera relatório de custos de um projeto específico pelo ID.
     * 
     * @param projectId ID do projeto
     * @return Lista com dados de custo do projeto específico ou lista vazia se não encontrado
     */
    public List<CostReportDto> getCostReportById(Long projectId) {
        return projectRepository.findById(projectId)
            .map(project -> {
                BigDecimal totalBudget = project.getTotalBudget() != null ? project.getTotalBudget() : BigDecimal.ZERO;
                BigDecimal totalCosts = project.getRealizedCost() != null ? project.getRealizedCost() : BigDecimal.ZERO;
                
                // Separar custos por categoria (aproximação melhorada baseada em dados reais)
                BigDecimal materialCosts = totalCosts.multiply(BigDecimal.valueOf(0.6)); // 60% material
                BigDecimal serviceCosts = totalCosts.multiply(BigDecimal.valueOf(0.4));   // 40% serviço
                
                BigDecimal remainingBudget = totalBudget.subtract(totalCosts);
                Double budgetUtilizationPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0 ? 
                    totalCosts.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
                Boolean overBudget = totalCosts.compareTo(totalBudget) > 0;
                
                CostReportDto dto = new CostReportDto(
                    project.getId(),
                    project.getName(),
                    project.getClient(),
                    totalBudget,
                    materialCosts,
                    serviceCosts,
                    totalCosts,
                    remainingBudget,
                    budgetUtilizationPercent,
                    overBudget,
                    LocalDate.now()
                );
                
                return List.of(dto);
            })
            .orElse(List.of());
    }

    /**
     * Gera relatório de um material específico pelo ID.
     * 
     * @param materialId ID do material
     * @return Lista com dados do material específico ou lista vazia se não encontrado
     */
    public List<StockReportDto> getStockReportById(Long materialId) {
        return materialRepository.findById(materialId)
            .map(material -> {
                BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
                BigDecimal minimumStock = material.getMinimumStock() != null ? material.getMinimumStock() : BigDecimal.ZERO;
                Boolean lowStock = currentStock.compareTo(minimumStock) <= 0;
                BigDecimal totalValue = currentStock.multiply(material.getUnitPrice());
                
                StockReportDto dto = new StockReportDto(
                    material.getId(),
                    material.getName(),
                    "Geral", // Categoria não definida na entidade
                    material.getUnitOfMeasure(),
                    currentStock.intValue(),
                    minimumStock.intValue(),
                    lowStock,
                    material.getUnitPrice(),
                    totalValue,
                    LocalDate.now(),
                    material.getSupplier()
                );
                
                return List.of(dto);
            })
            .orElse(List.of());
    }

    /**
     * Gera relatório consolidado de todos os projetos com informações detalhadas.
     * 
     * O relatório inclui para cada projeto:
     * - Informações básicas (nome, cliente, datas, descrição)
     * - Status e progresso calculado
     * - Contagem de tarefas (total vs concluídas)
     * - Lista detalhada de tarefas com custos e cronograma
     * - Equipe do projeto e performance dos membros
     * - Indicadores de atraso e performance
     * - Análise detalhada de custos por categoria
     * - Métricas de produtividade e eficiência
     * 
     * @return Lista de ProjectReportDto com dados detalhados
     */
    public List<ProjectReportDto> getProjectReport() {
        List<Project> projects = projectRepository.findAll();
        
        return projects.stream().map(this::buildDetailedProjectReport)
                .collect(Collectors.toList());
    }

    /**
     * Gera relatório de custos por projeto.
     * 
     * Análise financeira incluindo:
     * - Orçamento total vs gastos realizados
     * - Separação por categoria (material/serviço aproximada)
     * - Percentual de utilização do orçamento
     * - Valor restante disponível
     * - Indicador de estouro orçamentário
     * 
     * NOTA: A separação material/serviço usa aproximação percentual
     * até implementação de categorização detalhada de custos.
     * 
     * @return Lista de CostReportDto com análise financeira
     */
    public List<CostReportDto> getCostReport() {
        List<Project> projects = projectRepository.findAll();
        
        return projects.stream().map(project -> {
            BigDecimal totalBudget = project.getTotalBudget() != null ? project.getTotalBudget() : BigDecimal.ZERO;
            BigDecimal totalCosts = project.getRealizedCost() != null ? project.getRealizedCost() : BigDecimal.ZERO;
            
            // Separar custos por categoria (aproximação)
            BigDecimal materialCosts = totalCosts.multiply(BigDecimal.valueOf(0.6)); // 60% material
            BigDecimal serviceCosts = totalCosts.multiply(BigDecimal.valueOf(0.4));   // 40% serviço
            
            BigDecimal remainingBudget = totalBudget.subtract(totalCosts);
            Double budgetUtilizationPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0 ? 
                totalCosts.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
            Boolean overBudget = totalCosts.compareTo(totalBudget) > 0;
            
            return new CostReportDto(
                project.getId(),
                project.getName(),
                project.getClient(),
                totalBudget,
                materialCosts,
                serviceCosts,
                totalCosts,
                remainingBudget,
                budgetUtilizationPercent,
                overBudget,
                LocalDate.now()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Gera relatório de situação do estoque.
     * 
     * Para cada material, o relatório mostra:
     * - Quantidade atual vs mínima
     * - Indicador de baixo estoque
     * - Valor total do material em estoque
     * - Informações do fornecedor
     * - Data de referência do relatório
     * 
     * Essencial para:
     * - Controle de reposição de materiais
     * - Análise de valor imobilizado
     * - Planejamento de compras
     * 
     * @return Lista de StockReportDto com situação atual do estoque
     */
    public List<StockReportDto> getStockReport() {
        List<Material> materials = materialRepository.findAll();
        
        return materials.stream().map(material -> {
            BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal minimumStock = material.getMinimumStock() != null ? material.getMinimumStock() : BigDecimal.ZERO;
            Boolean lowStock = currentStock.compareTo(minimumStock) <= 0;
            BigDecimal totalValue = currentStock.multiply(material.getUnitPrice());
            
            return new StockReportDto(
                material.getId(),
                material.getName(),
                "Geral", // Categoria não definida na entidade
                material.getUnitOfMeasure(),
                currentStock.intValue(),
                minimumStock.intValue(),
                lowStock,
                material.getUnitPrice(),
                totalValue,
                LocalDate.now(),
                material.getSupplier()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Calcula o percentual de progresso de um projeto.
     * 
     * Lógica de cálculo:
     * 1. Se há tarefas: percentual baseado em tarefas concluídas
     * 2. Se não há tarefas: usa progresso manual do projeto
     * 3. Padrão: 0% se não há dados
     * 
     * @param project projeto para calcular progresso
     * @param totalTasks número total de tarefas
     * @param completedTasks número de tarefas concluídas
     * @return percentual de progresso (0-100)
     */
    private Integer calculateProgressPercentage(Project project, Integer totalTasks, Integer completedTasks) {
        if (totalTasks == 0) {
            BigDecimal projectProgress = project.getProgressPercentage();
            return projectProgress != null ? projectProgress.intValue() : 0;
        }
        return (completedTasks * 100) / totalTasks;
    }

    /**
     * Verifica se um projeto está atrasado.
     * 
     * Critérios para atraso:
     * - Data planejada de conclusão definida
     * - Data atual após a data planejada
     * - Projeto ainda não concluído (sem data real de conclusão)
     * 
     * @param project projeto a verificar
     * @return true se projeto está atrasado, false caso contrário
     */
    private Boolean isProjectDelayed(Project project) {
        if (project.getEndDatePlanned() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return today.isAfter(project.getEndDatePlanned()) && 
               project.getEndDateActual() == null;
    }

    /**
     * Calcula dias restantes para conclusão planejada.
     * 
     * Regras de cálculo:
     * - Se projeto já concluído: 0 dias
     * - Se sem data planejada: 0 dias
     * - Diferença entre hoje e data planejada (pode ser negativo se atrasado)
     * 
     * @param project projeto para calcular dias restantes
     * @return número de dias até conclusão planejada
     */
    private Long calculateDaysRemaining(Project project) {
        if (project.getEndDatePlanned() == null || project.getEndDateActual() != null) {
            return 0L;
        }
        LocalDate today = LocalDate.now();
        return ChronoUnit.DAYS.between(today, project.getEndDatePlanned());
    }

    /**
     * Constrói relatório detalhado de um projeto incluindo tarefas e equipe.
     * 
     * @param project projeto para gerar relatório
     * @return ProjectReportDto com informações completas
     */
    private ProjectReportDto buildDetailedProjectReport(Project project) {
        // Buscar tarefas do projeto
        List<Task> projectTasks = taskRepository.findByProjectId(project.getId());
        
        // Calcular métricas básicas
        Integer totalTasks = projectTasks.size();
        Integer completedTasks = (int) projectTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.CONCLUIDA)
            .count();
        
        Integer progressPercentage = calculateProgressPercentage(project, totalTasks, completedTasks);
        Boolean delayed = isProjectDelayed(project);
        Long daysRemaining = calculateDaysRemaining(project);
        
        // Construir lista de tarefas detalhadas
        List<TaskSummaryDto> taskSummaries = buildTaskSummaries(projectTasks);
        
        // Construir lista de membros da equipe
        List<TeamMemberDto> teamMembers = buildTeamMemberList(project, projectTasks);
        
        // Calcular resumo de status das tarefas
        ProjectReportDto.TaskStatusSummary taskStatusSummary = buildTaskStatusSummary(projectTasks);
        
        // Calcular detalhamento de custos
        ProjectReportDto.ProjectCostBreakdown costBreakdown = buildCostBreakdown(projectTasks);
        
        // Calcular métricas de performance
        ProjectReportDto.ProjectPerformanceMetrics performanceMetrics = buildPerformanceMetrics(project, projectTasks);
        
        // Criar DTO com todas as informações
        ProjectReportDto dto = new ProjectReportDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setClient(project.getClient());
        dto.setStartDatePlanned(project.getStartDatePlanned());
        dto.setEndDatePlanned(project.getEndDatePlanned());
        dto.setStartDateActual(project.getStartDateActual());
        dto.setEndDateActual(project.getEndDateActual());
        dto.setStatus(project.getStatus().getDescription());
        dto.setProgressPercentage(progressPercentage);
        dto.setTotalBudget(project.getTotalBudget());
        dto.setUsedBudget(project.getRealizedCost());
        dto.setTotalTasks(totalTasks);
        dto.setCompletedTasks(completedTasks);
        dto.setDelayed(delayed);
        dto.setDaysRemaining(daysRemaining);
        dto.setDescription(project.getDescription());
        dto.setCreatedByName(project.getCreatedBy() != null ? project.getCreatedBy().getFullName() : "N/A");
        dto.setCreatedAt(project.getCreatedAt() != null ? project.getCreatedAt().toLocalDate() : null);
        dto.setTeamSize(project.getTeamMembers().size());
        dto.setTeamMembers(teamMembers);
        dto.setTasks(taskSummaries);
        dto.setTaskStatusSummary(taskStatusSummary);
        dto.setCostBreakdown(costBreakdown);
        dto.setPerformanceMetrics(performanceMetrics);
        
        return dto;
    }

    /**
     * Constrói lista de resumos das tarefas do projeto.
     */
    private List<TaskSummaryDto> buildTaskSummaries(List<Task> tasks) {
        return tasks.stream().map(task -> {
            TaskSummaryDto summary = new TaskSummaryDto();
            summary.setId(task.getId());
            summary.setTitle(task.getTitle());
            summary.setStatus(task.getStatus().name());
            summary.setProgressPercentage(task.getProgressPercentage());
            summary.setPriority(task.getPriority());
            summary.setPriorityDescription(task.getPriorityDescription());
            summary.setStartDatePlanned(task.getStartDatePlanned());
            summary.setEndDatePlanned(task.getEndDatePlanned());
            summary.setStartDateActual(task.getStartDateActual());
            summary.setEndDateActual(task.getEndDateActual());
            summary.setEstimatedHours(task.getEstimatedHours());
            summary.setActualHours(task.getActualHours());
            summary.setAssignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : "Não atribuído");
            summary.setLaborCost(task.getLaborCost());
            summary.setMaterialCost(task.getMaterialCost());
            summary.setEquipmentCost(task.getEquipmentCost());
            summary.setTotalCost(task.getTotalCost());
            summary.setIsOverdue(task.isOverdue());
            
            // Calcular dias de atraso se aplicável
            if (task.isOverdue() && task.getEndDatePlanned() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(task.getEndDatePlanned(), LocalDate.now());
                summary.setDaysOverdue(daysOverdue);
            } else {
                summary.setDaysOverdue(0L);
            }
            
            return summary;
        }).collect(Collectors.toList());
    }

    /**
     * Constrói lista de membros da equipe com estatísticas.
     */
    private List<TeamMemberDto> buildTeamMemberList(Project project, List<Task> tasks) {
        return project.getTeamMembers().stream().map(user -> {
            // Contar tarefas atribuídas e concluídas por usuário
            List<Task> userTasks = tasks.stream()
                .filter(task -> task.getAssignedUser() != null && task.getAssignedUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
            
            int assignedTasksCount = userTasks.size();
            int completedTasksCount = (int) userTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.CONCLUIDA)
                .count();
            
            double completionRate = assignedTasksCount > 0 ? 
                (double) completedTasksCount / assignedTasksCount * 100 : 0.0;
            
            TeamMemberDto member = new TeamMemberDto();
            member.setId(user.getId());
            member.setFullName(user.getFullName());
            member.setEmail(user.getEmail());
            member.setRole(user.getRoles() != null && !user.getRoles().isEmpty() ? 
                user.getRoles().iterator().next().getName() : "N/A");
            member.setAssignedTasksCount(assignedTasksCount);
            member.setCompletedTasksCount(completedTasksCount);
            member.setTaskCompletionRate(completionRate);
            member.setStatus(user.isActive() ? "ATIVO" : "INATIVO");
            
            return member;
        }).collect(Collectors.toList());
    }

    /**
     * Constrói resumo de tarefas por status.
     */
    private ProjectReportDto.TaskStatusSummary buildTaskStatusSummary(List<Task> tasks) {
        int todoTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.A_FAZER).count();
        int inProgressTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.EM_ANDAMENTO).count();
        int completedTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.CONCLUIDA).count();
        int overdueTasks = (int) tasks.stream().filter(Task::isOverdue).count();
        
        return new ProjectReportDto.TaskStatusSummary(todoTasks, inProgressTasks, completedTasks, overdueTasks);
    }

    /**
     * Constrói detalhamento de custos por categoria.
     */
    private ProjectReportDto.ProjectCostBreakdown buildCostBreakdown(List<Task> tasks) {
        BigDecimal totalLaborCost = tasks.stream()
            .map(Task::getLaborCost)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalMaterialCost = tasks.stream()
            .map(Task::getMaterialCost)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalEquipmentCost = tasks.stream()
            .map(Task::getEquipmentCost)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalCost = totalLaborCost.add(totalMaterialCost).add(totalEquipmentCost);
        
        // Calcular percentuais
        double laborPercentage = 0.0;
        double materialPercentage = 0.0;
        double equipmentPercentage = 0.0;
        
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            laborPercentage = totalLaborCost.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
            materialPercentage = totalMaterialCost.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
            equipmentPercentage = totalEquipmentCost.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        
        return new ProjectReportDto.ProjectCostBreakdown(
            totalLaborCost, totalMaterialCost, totalEquipmentCost,
            laborPercentage, materialPercentage, equipmentPercentage
        );
    }

    /**
     * Constrói métricas de performance do projeto.
     */
    private ProjectReportDto.ProjectPerformanceMetrics buildPerformanceMetrics(Project project, List<Task> tasks) {
        // Variação do cronograma
        Long scheduleVariance = null;
        if (project.getEndDatePlanned() != null && project.getEndDateActual() != null) {
            scheduleVariance = ChronoUnit.DAYS.between(project.getEndDatePlanned(), project.getEndDateActual());
        }
        
        // Variação do orçamento
        BigDecimal budgetVariance = BigDecimal.ZERO;
        if (project.getTotalBudget() != null && project.getRealizedCost() != null) {
            budgetVariance = project.getTotalBudget().subtract(project.getRealizedCost());
        }
        
        // Eficiência da equipe (baseada em conclusão de tarefas)
        double teamEfficiency = 0.0;
        if (!tasks.isEmpty()) {
            long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.CONCLUIDA).count();
            teamEfficiency = (double) completedTasks / tasks.size() * 100;
        }
        
        // Taxa de conclusão no prazo
        double onTimeCompletionRate = 0.0;
        List<Task> completedTasks = tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.CONCLUIDA)
            .collect(Collectors.toList());
        
        if (!completedTasks.isEmpty()) {
            long onTimeTasks = completedTasks.stream()
                .filter(task -> !task.isOverdue())
                .count();
            onTimeCompletionRate = (double) onTimeTasks / completedTasks.size() * 100;
        }
        
        // Produtividade (tarefas concluídas por dia)
        double productivity = 0.0;
        if (project.getStartDateActual() != null && !completedTasks.isEmpty()) {
            long daysSinceStart = ChronoUnit.DAYS.between(project.getStartDateActual(), LocalDate.now());
            if (daysSinceStart > 0) {
                productivity = (double) completedTasks.size() / daysSinceStart;
            }
        }
        
        // Variação de horas estimadas vs realizadas
        double hoursVariancePercentage = 0.0;
        Integer totalEstimatedHours = tasks.stream()
            .mapToInt(task -> task.getEstimatedHours() != null ? task.getEstimatedHours() : 0)
            .sum();
        Integer totalActualHours = tasks.stream()
            .mapToInt(task -> task.getActualHours() != null ? task.getActualHours() : 0)
            .sum();
        
        if (totalEstimatedHours > 0) {
            hoursVariancePercentage = (double) totalActualHours / totalEstimatedHours * 100;
        }
        
        return new ProjectReportDto.ProjectPerformanceMetrics(
            scheduleVariance, budgetVariance, teamEfficiency, 
            onTimeCompletionRate, productivity, hoursVariancePercentage
        );
    }
} 