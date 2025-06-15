package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.DashboardDto;
import br.com.sgpc.sgpc_api.entity.Material;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import br.com.sgpc.sgpc_api.enums.RequestStatus;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;
import br.com.sgpc.sgpc_api.repository.MaterialRequestRepository;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    @Autowired
    private MaterialRepository materialRepository;

    public DashboardDto getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        // Dados de projetos
        Long totalProjectsLong = projectRepository.count();
        Integer totalProjects = totalProjectsLong.intValue();
        
        Long activeProjectsLong = projectRepository.countByStatus(ProjectStatus.EM_ANDAMENTO);
        Integer activeProjects = activeProjectsLong != null ? activeProjectsLong.intValue() : 0;
        
        Integer delayedProjects = projectRepository.findDelayedProjects(today).size();

        // Dados de tarefas - usando consultas customizadas baseadas nos métodos disponíveis
        List<Task> pendingTasksList = taskRepository.findOverdueTasks(today);
        Integer pendingTasks = pendingTasksList.size();
        
        // Aproximação para tarefas concluídas no mês - usando data range
        List<Task> tasksInMonth = taskRepository.findTasksInDateRange(firstDayOfMonth, today);
        Integer completedTasksThisMonth = (int) tasksInMonth.stream()
            .filter(t -> t.getStatus() == TaskStatus.CONCLUIDA)
            .count();

        // Dados de requisições de material
        Integer pendingMaterialRequests = materialRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDENTE).size();

        // Dados de estoque baixo - aproximação usando todos os materiais
        List<Material> allMaterials = materialRepository.findAll();
        Integer lowStockAlerts = (int) allMaterials.stream()
            .filter(m -> m.getCurrentStock() != null && m.getMinimumStock() != null && 
                        m.getCurrentStock().compareTo(m.getMinimumStock()) <= 0)
            .count();

        // Dados orçamentários - calculados manualmente
        List<Project> allProjects = projectRepository.findAll();
        BigDecimal totalBudget = allProjects.stream()
            .filter(p -> p.getTotalBudget() != null)
            .map(Project::getTotalBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal usedBudget = allProjects.stream()
            .filter(p -> p.getRealizedCost() != null)
            .map(Project::getRealizedCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Projetos criados neste mês - aproximação usando data range
        List<Project> projectsInRange = projectRepository.findProjectsInDateRange(firstDayOfMonth, today);
        Integer newProjectsThisMonth = projectsInRange.size();

        return new DashboardDto(
            totalProjects,
            activeProjects,
            delayedProjects,
            pendingTasks,
            pendingMaterialRequests,
            lowStockAlerts,
            totalBudget,
            usedBudget,
            completedTasksThisMonth,
            newProjectsThisMonth
        );
    }
} 