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
import br.com.sgpc.sgpc_api.exception.InvalidDateException;
import br.com.sgpc.sgpc_api.exception.ProjectAlreadyExistsException;
import br.com.sgpc.sgpc_api.exception.ProjectNotFoundException;
import br.com.sgpc.sgpc_api.exception.UserAlreadyInTeamException;
import br.com.sgpc.sgpc_api.exception.UserNotFoundException;
import br.com.sgpc.sgpc_api.exception.UserNotInTeamException;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.TaskServiceRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

/**
 * Serviço responsável pela lógica de negócio dos projetos.
 * 
 * Esta classe implementa todas as operações relacionadas ao gerenciamento
 * de projetos de construção, incluindo CRUD, gerenciamento de equipes,
 * controle de orçamento e cálculos de progresso.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskServiceRepository taskServiceRepository;

    /**
     * Cria um novo projeto no sistema.
     * 
     * Valida se não existe outro projeto com o mesmo nome, cria a entidade
     * Project com os dados fornecidos e associa os membros da equipe especificados.
     * 
     * @param projectCreateDto dados para criação do projeto
     * @return ProjectDetailsDto dados completos do projeto criado
     * @throws ProjectAlreadyExistsException se já existir projeto com o mesmo nome
     * @throws RuntimeException se usuário não for encontrado
     */
    public ProjectDetailsDto createProject(ProjectCreateDto projectCreateDto) {
        if (projectRepository.existsByName(projectCreateDto.getName())) {
            throw new ProjectAlreadyExistsException("Já existe um projeto cadastrado com o nome: " + projectCreateDto.getName());
        }

        // Validar data inicial se fornecida - GP-30
        if (projectCreateDto.getStartDatePlanned() != null && 
            projectCreateDto.getStartDatePlanned().isBefore(LocalDate.now())) {
            throw new InvalidDateException("Data inicial planejada não pode ser anterior à data atual");
        }
        
        if (projectCreateDto.getStartDateActual() != null && 
            projectCreateDto.getStartDateActual().isBefore(LocalDate.now())) {
            throw new InvalidDateException("Data inicial real não pode ser anterior à data atual");
        }

        // Validar se data final não é anterior à data inicial - GP-30
        if (projectCreateDto.getStartDatePlanned() != null && 
            projectCreateDto.getEndDatePlanned() != null && 
            projectCreateDto.getEndDatePlanned().isBefore(projectCreateDto.getStartDatePlanned())) {
            throw new InvalidDateException("Data final planejada não pode ser anterior à data inicial planejada");
        }
        
        if (projectCreateDto.getStartDateActual() != null && 
            projectCreateDto.getEndDateActual() != null && 
            projectCreateDto.getEndDateActual().isBefore(projectCreateDto.getStartDateActual())) {
            throw new InvalidDateException("Data final real não pode ser anterior à data inicial real");
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
                        .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + userId + " não foi encontrado"));
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

    /**
     * Atualiza os dados de um projeto existente.
     * 
     * Permite atualização parcial dos dados do projeto. Valida se o novo nome
     * não está em uso por outro projeto antes de alterar.
     * 
     * @param id identificador único do projeto
     * @param projectUpdateDto dados para atualização (apenas campos não nulos são atualizados)
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws ProjectAlreadyExistsException se nome já existir
     * @throws RuntimeException se usuário não for encontrado
     */
    public ProjectDetailsDto updateProject(Long id, ProjectUpdateDto projectUpdateDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + id + " não foi encontrado"));

        // Validar se o nome já existe (apenas se for diferente do atual)
        if (projectUpdateDto.getName() != null && 
            !project.getName().equals(projectUpdateDto.getName()) &&
            projectRepository.existsByName(projectUpdateDto.getName())) {
            throw new ProjectAlreadyExistsException("Já existe um projeto cadastrado com o nome: " + projectUpdateDto.getName());
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
                        .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + userId + " não foi encontrado"));
                newTeamMembers.add(user);
            }
            project.setTeamMembers(newTeamMembers);
        }

        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    /**
     * Remove um projeto do sistema.
     * 
     * @param id ID do projeto a ser removido
     * @throws ProjectNotFoundException se o projeto não for encontrado
     */
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + id + " não foi encontrado"));
        
        projectRepository.delete(project);
    }

    /**
     * Adiciona um membro à equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws RuntimeException se usuário não for encontrado
     * @throws UserAlreadyInTeamException se usuário já estiver na equipe
     */
    public ProjectDetailsDto addTeamMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + userId + " não foi encontrado"));

        if (project.hasTeamMember(user)) {
            throw new UserAlreadyInTeamException("O usuário já faz parte da equipe do projeto");
        }

        project.addTeamMember(user);
        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    /**
     * Remove um membro da equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws RuntimeException se usuário não for encontrado
     * @throws UserNotInTeamException se usuário não estiver na equipe
     */
    public ProjectDetailsDto removeTeamMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + userId + " não foi encontrado"));

        if (!project.hasTeamMember(user)) {
            throw new UserNotInTeamException("O usuário não faz parte da equipe do projeto");
        }

        project.removeTeamMember(user);
        Project savedProject = projectRepository.save(project);
        return convertToDetailsDto(savedProject);
    }

    /**
     * Lista membros da equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @return List<UserDto> lista de membros da equipe
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    @Transactional(readOnly = true)
    public List<UserDto> getProjectTeamMembers(Long projectId) {
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        return project.getTeamMembers().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    // Métodos para gestão de orçamento e custos (RF09)

    /**
     * Obtém informações de orçamento de um projeto.
     * 
     * Calcula e retorna informações detalhadas sobre orçamento planejado,
     * custos realizados, percentual de utilização e estimativas de conclusão.
     * 
     * @param projectId ID do projeto
     * @return ProjectBudgetDto informações de orçamento do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    @Transactional(readOnly = true)
    public ProjectBudgetDto getProjectBudget(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        ProjectBudgetDto budgetDto = new ProjectBudgetDto();
        budgetDto.setProjectId(projectId);
        budgetDto.setProjectName(project.getName());
        budgetDto.setTotalBudget(project.getTotalBudget());
        budgetDto.setRealizedCost(project.getRealizedCost() != null ? project.getRealizedCost() : BigDecimal.ZERO);
        
        // Calcular variação do orçamento
        BigDecimal budgetVariance = project.getTotalBudget().subtract(budgetDto.getRealizedCost());
        budgetDto.setBudgetVariance(budgetVariance);
        
        // Calcular percentual utilizado
        if (project.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = budgetDto.getRealizedCost()
                    .divide(project.getTotalBudget(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            budgetDto.setBudgetUsagePercentage(percentage);
        } else {
            budgetDto.setBudgetUsagePercentage(BigDecimal.ZERO);
        }

        // Definir progresso do projeto
        budgetDto.setProgressPercentage(project.getProgressPercentage() != null ? 
                                      project.getProgressPercentage() : BigDecimal.ZERO);

        // Determinar se está acima do orçamento
        budgetDto.setIsOverBudget(budgetVariance.compareTo(BigDecimal.ZERO) < 0);

        return budgetDto;
    }

    /**
     * Recalcula o custo realizado de um projeto baseado nos custos das tarefas.
     * 
     * @param projectId ID do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    public void recalculateProjectRealizedCost(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

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

    /**
     * Recalcula o progresso de um projeto baseado no progresso das tarefas.
     * 
     * @param projectId ID do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    public void recalculateProjectProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

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

    /**
     * Verifica se o projeto está acima do orçamento.
     * 
     * @param projectId ID do projeto
     * @return true se o projeto está acima do orçamento
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    @Transactional(readOnly = true)
    public boolean isProjectOverBudget(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        if (project.getRealizedCost() == null || project.getTotalBudget() == null) {
            return false;
        }

        return project.getRealizedCost().compareTo(project.getTotalBudget()) > 0;
    }

    /**
     * Converte Project para ProjectDetailsDto.
     * 
     * @param project entidade do projeto
     * @return ProjectDetailsDto dados detalhados do projeto
     */
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
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Converter membros da equipe
        if (project.getTeamMembers() != null) {
            dto.setTeamMembers(project.getTeamMembers().stream()
                    .map(this::convertUserToDto)
                    .collect(Collectors.toSet()));
        }

        // Calcular dias de atraso se aplicável
        if (project.getEndDatePlanned() != null && project.getStatus() != ProjectStatus.CONCLUIDO) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(project.getEndDatePlanned())) {
                long daysOverdue = ChronoUnit.DAYS.between(project.getEndDatePlanned(), today);
                // Adicionar informação de atraso na descrição ou usar outro campo se disponível
            }
        }

        return dto;
    }

    /**
     * Converte Project para ProjectSummaryDto.
     * 
     * @param project entidade do projeto
     * @return ProjectSummaryDto dados resumidos do projeto
     */
    private ProjectSummaryDto convertToSummaryDto(Project project) {
        ProjectSummaryDto dto = new ProjectSummaryDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setClient(project.getClient());
        dto.setStatus(project.getStatus());
        dto.setStartDatePlanned(project.getStartDatePlanned());
        dto.setEndDatePlanned(project.getEndDatePlanned());
        dto.setTotalBudget(project.getTotalBudget());
        dto.setCreatedAt(project.getCreatedAt());

        // Calcular progresso percentual
        if (project.getProgressPercentage() != null) {
            dto.setProgressPercentage(project.getProgressPercentage().intValue());
        } else {
            dto.setProgressPercentage(0);
        }

        // Calcular tamanho da equipe usando query específica para evitar lazy loading
        Long teamSize = projectRepository.countTeamMembersByProjectId(project.getId());
        dto.setTeamSize(teamSize != null ? teamSize.intValue() : 0);

        return dto;
    }

    /**
     * Converte User para UserDto.
     * 
     * @param user entidade do usuário
     * @return UserDto dados do usuário
     */
    private UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
} 