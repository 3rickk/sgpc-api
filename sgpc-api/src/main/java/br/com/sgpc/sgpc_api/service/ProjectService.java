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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import br.com.sgpc.sgpc_api.security.UserDetailsImpl;

/**
 * Serviço responsável pela lógica de negócio dos projetos.
 * 
 * Esta classe implementa todas as operações relacionadas ao gerenciamento
 * de projetos de construção, incluindo CRUD, gerenciamento de equipes,
 * controle de orçamento, cálculos de progresso e isolamento de dados
 * por usuário/contexto.
 * 
 * Implementa isolamento de dados onde:
 * - ADMIN: acessa projetos que criou ou está na equipe
 * - MANAGER: acessa apenas projetos onde está na equipe
 * - USER: acessa apenas projetos onde está na equipe
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
     * Obtém informações do usuário logado.
     * 
     * @return User usuário autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário logado não encontrado"));
    }

    /**
     * Obtém a role principal do usuário logado.
     * 
     * @return String role do usuário (ADMIN, MANAGER, USER)
     */
    private String getCurrentUserRole() {
        User user = getCurrentUser();
        if (user.hasRole("ADMIN")) return "ADMIN";
        if (user.hasRole("MANAGER")) return "MANAGER";
        return "USER";
    }

    /**
     * Cria um novo projeto no sistema.
     * 
     * Valida se não existe outro projeto com o mesmo nome, cria a entidade
     * Project com os dados fornecidos, define o usuário logado como criador
     * e associa os membros da equipe especificados.
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

        User currentUser = getCurrentUser();

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
        
        // Define o usuário logado como criador do projeto
        project.setCreatedBy(currentUser);

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

    /**
     * Lista todos os projetos acessíveis pelo usuário logado.
     * 
     * Aplica filtros baseados na role do usuário:
     * - ADMIN: projetos que criou ou está na equipe
     * - MANAGER/USER: apenas projetos onde está na equipe
     * 
     * @return List<ProjectSummaryDto> projetos acessíveis
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getAllProjects() {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findProjectsAccessibleByUser(currentUser.getId(), userRole)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um projeto por ID com verificação de acesso.
     * 
     * @param id ID do projeto
     * @return Optional<ProjectDetailsDto> projeto se acessível pelo usuário
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDetailsDto> getProjectById(Long id) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findByIdAccessibleByUser(id, currentUser.getId(), userRole)
                .map(this::convertToDetailsDto);
    }

    /**
     * Busca projetos por status acessíveis pelo usuário logado.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByStatus(ProjectStatus status) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findByStatusAccessibleByUser(status, currentUser.getId(), userRole)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca projetos por cliente acessíveis pelo usuário logado.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByClient(String client) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findByClientAccessibleByUser(client, currentUser.getId(), userRole)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca projetos por nome acessíveis pelo usuário logado.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> searchProjectsByName(String name) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findByNameContainingIgnoreCaseAccessibleByUser(name, currentUser.getId(), userRole)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca projetos por usuário (apenas para ADMIN e MANAGER).
     * USER pode ver apenas seus próprios projetos.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getProjectsByUserId(Long userId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // USER só pode ver seus próprios projetos
        if ("USER".equals(userRole) && !currentUser.getId().equals(userId)) {
            throw new SecurityException("Usuário não tem permissão para ver projetos de outros usuários");
        }
        
        return projectRepository.findProjectsByTeamMemberId(userId).stream()
                .filter(project -> {
                    // Verifica se o projeto atual é acessível pelo usuário logado
                    return projectRepository.isProjectAccessibleByUser(project.getId(), currentUser.getId(), userRole);
                })
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca projetos atrasados acessíveis pelo usuário logado.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getDelayedProjects() {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.findDelayedProjectsAccessibleByUser(LocalDate.now(), currentUser.getId(), userRole)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário logado pode acessar o projeto.
     */
    public boolean isUserInProjectTeam(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário pode acessar o projeto
        return projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole);
    }

    /**
     * Atualiza os dados de um projeto existente.
     * 
     * Permite atualização parcial dos dados do projeto. Valida se o novo nome
     * não está em uso por outro projeto antes de alterar.
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param id identificador único do projeto
     * @param projectUpdateDto dados para atualização (apenas campos não nulos são atualizados)
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws ProjectAlreadyExistsException se nome já existir
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    public ProjectDetailsDto updateProject(Long id, ProjectUpdateDto projectUpdateDto) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(id, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
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
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param id ID do projeto a ser removido
     * @throws ProjectNotFoundException se o projeto não for encontrado
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(id, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + id + " não foi encontrado"));
        
        projectRepository.delete(project);
    }

    /**
     * Adiciona um membro à equipe do projeto.
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws UserNotFoundException se usuário não for encontrado
     * @throws UserAlreadyInTeamException se usuário já estiver na equipe
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    public ProjectDetailsDto addTeamMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
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
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ProjectDetailsDto dados atualizados do projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws UserNotFoundException se usuário não for encontrado
     * @throws UserNotInTeamException se usuário não estiver na equipe
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    public ProjectDetailsDto removeTeamMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
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
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param projectId ID do projeto
     * @return List<UserDto> lista de membros da equipe
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    @Transactional(readOnly = true)
    public List<UserDto> getProjectTeamMembers(Long projectId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        Project project = projectRepository.findByIdWithTeamMembers(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        return project.getTeamMembers().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    // Métodos para gestão de orçamento e custos (RF09)

    /**
     * Obtém informações de orçamento de um projeto.
     * Verifica se o usuário tem acesso ao projeto.
     * 
     * @param projectId ID do projeto
     * @return ProjectBudgetDto informações de orçamento
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    @Transactional(readOnly = true)
    public ProjectBudgetDto getProjectBudget(Long projectId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se o usuário tem acesso ao projeto
        if (!projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        ProjectBudgetDto budgetDto = new ProjectBudgetDto();
        budgetDto.setProjectId(projectId);
        budgetDto.setProjectName(project.getName());
        budgetDto.setTotalBudget(project.getTotalBudget());
        budgetDto.setRealizedCost(project.getRealizedCost());
        
        if (project.getTotalBudget() != null && project.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variance = project.getTotalBudget().subtract(project.getRealizedCost());
            budgetDto.setBudgetVariance(variance);
            
            BigDecimal usagePercentage = project.getRealizedCost()
                    .divide(project.getTotalBudget(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            budgetDto.setBudgetUsagePercentage(usagePercentage);
            
            budgetDto.setIsOverBudget(project.getRealizedCost().compareTo(project.getTotalBudget()) > 0);
        } else {
            budgetDto.setBudgetVariance(BigDecimal.ZERO);
            budgetDto.setBudgetUsagePercentage(BigDecimal.ZERO);
            budgetDto.setIsOverBudget(false);
        }

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

        // Calcular custo total de todas as tarefas (mesma lógica do TaskService)
        // Isso inclui tanto custos diretos quanto custos de serviços das tarefas
        BigDecimal totalTaskCosts = taskRepository.findByProjectId(projectId)
                .stream()
                .map(Task::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        project.updateRealizedCost(totalTaskCosts);
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