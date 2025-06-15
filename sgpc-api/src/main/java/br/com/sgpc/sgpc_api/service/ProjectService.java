package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.ProjectBudgetDto;
import br.com.sgpc.sgpc_api.dto.ProjectCreateDto;
import br.com.sgpc.sgpc_api.dto.ProjectDetailsDto;
import br.com.sgpc.sgpc_api.dto.ProjectSummaryDto;
import br.com.sgpc.sgpc_api.dto.ProjectUpdateDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.TaskServiceRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskServiceRepository taskServiceRepository;

    public ProjectDetailsDto createProject(ProjectCreateDto projectCreateDto) {
        if (projectRepository.existsByName(projectCreateDto.getName())) {
            throw new RuntimeException("Já existe um projeto com este nome!");
        }

        Project project = new Project();
        project.setName(projectCreateDto.getName());
        project.setDescription(projectCreateDto.getDescription());
        project.setStartDatePlanned(projectCreateDto.getStartDatePlanned());
        project.setEndDatePlanned(projectCreateDto.getEndDatePlanned());
        project.setStartDateActual(projectCreateDto.getStartDateActual());
        project.setEndDateActual(projectCreateDto.getEndDateActual());
        project.setTotalBudget(projectCreateDto.getTotalBudget());
        project.setClient(projectCreateDto.getClient());
        project.setStatus(projectCreateDto.getStatus() != null ? 
                          projectCreateDto.getStatus() : ProjectStatus.PLANEJAMENTO);

        // Adicionar membros da equipe
        if (projectCreateDto.getTeamMemberIds() != null && !projectCreateDto.getTeamMemberIds().isEmpty()) {
            Set<User> teamMembers = new HashSet<>();
            for (Long userId : projectCreateDto.getTeamMemberIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
                teamMembers.add(user);
            }
            project.setTeamMembers(teamMembers);
        }

        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDetailsDto> getProjectById(Long id) {
        return projectRepository.findByIdWithTeamMembers(id)
                .map(this::convertToDetailsDto);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByClient(String client) {
        return projectRepository.findByClient(client).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> searchProjectsByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByUserId(Long userId) {
        return projectRepository.findProjectsByTeamMemberId(userId).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getDelayedProjects() {
        return projectRepository.findDelayedProjects(LocalDate.now()).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    public ProjectDetailsDto updateProject(Long id, ProjectUpdateDto projectUpdateDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        // Validar se o nome já existe (apenas se for diferente do atual)
        if (projectUpdateDto.getName() != null && 
            !project.getName().equals(projectUpdateDto.getName()) &&
            projectRepository.existsByName(projectUpdateDto.getName())) {
            throw new RuntimeException("Já existe um projeto com este nome!");
        }

        // Atualizar campos apenas se fornecidos
        if (projectUpdateDto.getName() != null) {
            project.setName(projectUpdateDto.getName());
        }
        if (projectUpdateDto.getDescription() != null) {
            project.setDescription(projectUpdateDto.getDescription());
        }
        if (projectUpdateDto.getStartDatePlanned() != null) {
            project.setStartDatePlanned(projectUpdateDto.getStartDatePlanned());
        }
        if (projectUpdateDto.getEndDatePlanned() != null) {
            project.setEndDatePlanned(projectUpdateDto.getEndDatePlanned());
        }
        if (projectUpdateDto.getStartDateActual() != null) {
            project.setStartDateActual(projectUpdateDto.getStartDateActual());
        }
        if (projectUpdateDto.getEndDateActual() != null) {
            project.setEndDateActual(projectUpdateDto.getEndDateActual());
        }
        if (projectUpdateDto.getTotalBudget() != null) {
            project.setTotalBudget(projectUpdateDto.getTotalBudget());
        }
        if (projectUpdateDto.getClient() != null) {
            project.setClient(projectUpdateDto.getClient());
        }
        if (projectUpdateDto.getStatus() != null) {
            project.setStatus(projectUpdateDto.getStatus());
        }

        // Atualizar equipe se fornecida
        if (projectUpdateDto.getTeamMemberIds() != null) {
            Set<User> newTeamMembers = new HashSet<>();
            for (Long userId : projectUpdateDto.getTeamMemberIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
                newTeamMembers.add(user);
            }
            project.setTeamMembers(newTeamMembers);
        }

        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
        
        projectRepository.delete(project);
    }

    public ProjectDetailsDto addTeamMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (project.hasTeamMember(user)) {
            throw new RuntimeException("Usuário já faz parte da equipe do projeto");
        }

        project.addTeamMember(user);
        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    public ProjectDetailsDto removeTeamMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!project.hasTeamMember(user)) {
            throw new RuntimeException("Usuário não faz parte da equipe do projeto");
        }

        project.removeTeamMember(user);
        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getProjectTeamMembers(Long projectId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        return project.getTeamMembers().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    // Métodos para gestão de orçamento e custos (RF09)
    @Transactional(readOnly = true)
    public ProjectBudgetDto getProjectBudget(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        ProjectBudgetDto budgetDto = new ProjectBudgetDto();
        budgetDto.setProjectId(project.getId());
        budgetDto.setProjectName(project.getName());
        budgetDto.setTotalBudget(project.getTotalBudget());
        budgetDto.setRealizedCost(project.getRealizedCost());
        budgetDto.setBudgetVariance(project.getBudgetVariance());
        budgetDto.setBudgetUsagePercentage(project.getBudgetUsagePercentage());
        budgetDto.setProgressPercentage(project.getProgressPercentage());
        budgetDto.setIsOverBudget(project.isOverBudget());

        // Calcular custos detalhados
        List<Task> projectTasks = taskRepository.findByProjectId(projectId);
        BigDecimal totalLaborCost = projectTasks.stream()
                .map(Task::getLaborCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMaterialCost = projectTasks.stream()
                .map(Task::getMaterialCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEquipmentCost = projectTasks.stream()
                .map(Task::getEquipmentCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budgetDto.setTotalLaborCost(totalLaborCost);
        budgetDto.setTotalMaterialCost(totalMaterialCost);
        budgetDto.setTotalEquipmentCost(totalEquipmentCost);

        // Estatísticas das tarefas
        budgetDto.setTotalTasks(projectTasks.size());
        budgetDto.setCompletedTasks((int) projectTasks.stream().filter(Task::isCompleted).count());
        budgetDto.setPendingTasks(budgetDto.getTotalTasks() - budgetDto.getCompletedTasks());

        return budgetDto;
    }

    // Recalcular custo realizado do projeto baseado nas tarefas concluídas
    public void recalculateProjectRealizedCost(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        // Calcular custo realizado com base nas tarefas concluídas
        BigDecimal realizedCost = taskServiceRepository.calculateRealizedCostByProjectId(projectId);
        
        // Adicionar custos diretos das tarefas concluídas (que não possuem serviços)
        BigDecimal taskDirectCosts = taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.CONCLUIDA)
                .stream()
                .map(Task::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRealizedCost = realizedCost.add(taskDirectCosts);

        project.updateRealizedCost(totalRealizedCost);
        projectRepository.save(project);
    }

    // Recalcular progresso do projeto baseado no progresso das tarefas
    public void recalculateProjectProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        List<Task> projectTasks = taskRepository.findByProjectId(projectId);

        if (projectTasks.isEmpty()) {
            project.updateProgress(BigDecimal.ZERO);
            projectRepository.save(project);
            return;
        }

        // Calcular progresso médio baseado no progresso das tarefas
        double averageProgress = projectTasks.stream()
                .mapToInt(Task::getProgressPercentage)
                .average()
                .orElse(0.0);

        project.updateProgress(BigDecimal.valueOf(averageProgress));
        projectRepository.save(project);
    }

    // Verificar se o projeto está acima do orçamento
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsOverBudget() {
        return projectRepository.findAll().stream()
                .filter(Project::isOverBudget)
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    // Relatório consolidado de orçamento de todos os projetos
    @Transactional(readOnly = true)
    public List<ProjectBudgetDto> getAllProjectsBudgetReport() {
        return projectRepository.findAll().stream()
                .map(project -> getProjectBudget(project.getId()))
                .collect(Collectors.toList());
    }

    private ProjectDetailsDto convertToDetailsDto(Project project) {
        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStartDatePlanned(project.getStartDatePlanned());
        dto.setEndDatePlanned(project.getEndDatePlanned());
        dto.setStartDateActual(project.getStartDateActual());
        dto.setEndDateActual(project.getEndDateActual());
        dto.setTotalBudget(project.getTotalBudget());
        dto.setClient(project.getClient());
        dto.setStatus(project.getStatus());
        dto.setStatusDescription(project.getStatus().getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        dto.setTeamSize(project.getTeamSize());
        
        // Converter membros da equipe
        if (project.getTeamMembers() != null) {
            Set<UserDto> teamMembersDto = project.getTeamMembers().stream()
                    .map(this::convertUserToDto)
                    .collect(Collectors.toSet());
            dto.setTeamMembers(teamMembersDto);
        }

        // Calcular campos derivados
        dto.setProgressPercentage(calculateProgressPercentage(project));
        dto.setIsDelayed(isProjectDelayed(project));
        dto.setDaysRemaining(calculateDaysRemaining(project));

        return dto;
    }

    private ProjectSummaryDto convertToSummaryDto(Project project) {
        ProjectSummaryDto dto = new ProjectSummaryDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setClient(project.getClient());
        dto.setStatus(project.getStatus());
        dto.setStatusDescription(project.getStatus().getDescription());
        dto.setStartDatePlanned(project.getStartDatePlanned());
        dto.setEndDatePlanned(project.getEndDatePlanned());
        dto.setTotalBudget(project.getTotalBudget());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setTeamSize(project.getTeamSize());
        dto.setProgressPercentage(calculateProgressPercentage(project));
        dto.setIsDelayed(isProjectDelayed(project));
        
        return dto;
    }

    private UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setHourlyRate(user.getHourlyRate());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }

    private Integer calculateProgressPercentage(Project project) {
        // Usar o progresso calculado do projeto se disponível
        if (project.getProgressPercentage() != null) {
            return project.getProgressPercentage().intValue();
        }

        // Lógica simplificada baseada no status
        switch (project.getStatus()) {
            case PLANEJAMENTO:
                return 0;
            case EM_ANDAMENTO:
                // Cálculo baseado nas datas (simplificado)
                if (project.getStartDateActual() != null && project.getEndDatePlanned() != null) {
                    LocalDate now = LocalDate.now();
                    if (now.isBefore(project.getStartDateActual())) return 0;
                    if (now.isAfter(project.getEndDatePlanned())) return 100;
                    
                    long totalDays = ChronoUnit.DAYS.between(project.getStartDateActual(), project.getEndDatePlanned());
                    long elapsedDays = ChronoUnit.DAYS.between(project.getStartDateActual(), now);
                    
                    if (totalDays > 0) {
                        return Math.min(100, (int) ((elapsedDays * 100) / totalDays));
                    }
                }
                return 50; // Default para em andamento
            case CONCLUIDO:
                return 100;
            case SUSPENSO:
            case CANCELADO:
                return 0;
            default:
                return 0;
        }
    }

    private Boolean isProjectDelayed(Project project) {
        if (project.getEndDatePlanned() == null) return false;
        
        LocalDate now = LocalDate.now();
        return now.isAfter(project.getEndDatePlanned()) && 
               !project.getStatus().equals(ProjectStatus.CONCLUIDO);
    }

    private Long calculateDaysRemaining(Project project) {
        if (project.getEndDatePlanned() == null) return null;
        
        LocalDate now = LocalDate.now();
        if (project.getStatus().equals(ProjectStatus.CONCLUIDO)) return 0L;
        
        return ChronoUnit.DAYS.between(now, project.getEndDatePlanned());
    }
} 