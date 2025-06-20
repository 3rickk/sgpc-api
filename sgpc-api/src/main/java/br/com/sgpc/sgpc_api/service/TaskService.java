package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.TaskCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskUpdateStatusDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.exception.ProjectNotFoundException;
import br.com.sgpc.sgpc_api.exception.TaskNotFoundException;
import br.com.sgpc.sgpc_api.exception.UserNotFoundException;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.security.UserDetailsImpl;

/**
 * Serviço responsável pela lógica de negócio das tarefas.
 * 
 * Esta classe implementa todas as operações relacionadas ao gerenciamento
 * de tarefas, incluindo CRUD, controle de status, atribuição de usuários,
 * cálculos de progresso e isolamento de dados por usuário/contexto.
 * 
 * Implementa isolamento de dados onde:
 * - ADMIN: acessa tarefas de projetos que criou ou está na equipe
 * - MANAGER: acessa tarefas de projetos onde está na equipe
 * - USER: acessa tarefas de projetos onde está na equipe, pode interagir apenas com tarefas que está atribuído
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

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
     * Verifica se o usuário pode acessar o projeto.
     * 
     * @param projectId ID do projeto
     * @return boolean true se pode acessar
     */
    private boolean canAccessProject(Long projectId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        return projectRepository.isProjectAccessibleByUser(projectId, currentUser.getId(), userRole);
    }

    /**
     * Verifica se o usuário pode editar uma tarefa.
     * Para USER: apenas tarefas que está atribuído
     * Para MANAGER/ADMIN: qualquer tarefa de projetos acessíveis
     * 
     * @param task tarefa a ser verificada
     * @return boolean true se pode editar
     */
    private boolean canEditTask(Task task) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // Verifica se pode acessar o projeto
        if (!canAccessProject(task.getProject().getId())) {
            return false;
        }
        
        // ADMIN e MANAGER podem editar qualquer tarefa do projeto
        if ("ADMIN".equals(userRole) || "MANAGER".equals(userRole)) {
            return true;
        }
        
        // USER só pode editar tarefas que está atribuído
        return task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId());
    }

    /**
     * Cria uma nova tarefa no sistema.
     * Verifica se o usuário pode acessar o projeto.
     * 
     * @param projectId ID do projeto ao qual a tarefa pertence
     * @param taskCreateDto dados da tarefa a ser criada
     * @param createdByUserId ID do usuário que está criando a tarefa
     * @return TaskViewDto dados da tarefa criada
     * @throws SecurityException se usuário não tiver acesso ao projeto
     * @throws ProjectNotFoundException se projeto não for encontrado
     * @throws RuntimeException se título já existir ou usuário não for encontrado
     */
    public TaskViewDto createTask(Long projectId, TaskCreateDto taskCreateDto, Long createdByUserId) {
        // Verificar se o usuário pode acessar o projeto
        if (!canAccessProject(projectId)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        // Verificar se o projeto existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        // Verificar se já existe uma tarefa com o mesmo título no projeto
        if (taskRepository.existsByProjectIdAndTitle(projectId, taskCreateDto.getTitle())) {
            throw new RuntimeException("Já existe uma tarefa com este título neste projeto!");
        }

        // Verificar se o usuário criador existe
        User createdByUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuário criador com ID " + createdByUserId + " não foi encontrado"));

        Task task = new Task();
        task.setTitle(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getDescription());
        task.setStatus(taskCreateDto.getStatus() != null ? taskCreateDto.getStatus() : TaskStatus.A_FAZER);
        task.setStartDatePlanned(taskCreateDto.getStartDatePlanned());
        task.setEndDatePlanned(taskCreateDto.getEndDatePlanned());
        task.setStartDateActual(taskCreateDto.getStartDateActual());
        task.setEndDateActual(taskCreateDto.getEndDateActual());
        task.setProgressPercentage(taskCreateDto.getProgressPercentage() != null ? taskCreateDto.getProgressPercentage() : 0);
        task.setPriority(taskCreateDto.getPriority() != null ? taskCreateDto.getPriority() : 1);
        task.setEstimatedHours(taskCreateDto.getEstimatedHours());
        task.setActualHours(taskCreateDto.getActualHours());
        task.setNotes(taskCreateDto.getNotes());
        task.setProject(project);
        task.setCreatedByUser(createdByUser);

        // Atribuir usuário responsável se fornecido
        if (taskCreateDto.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(taskCreateDto.getAssignedUserId())
                    .orElseThrow(() -> new UserNotFoundException("Usuário responsável com ID " + taskCreateDto.getAssignedUserId() + " não foi encontrado"));
            task.setAssignedUser(assignedUser);
        }

        Task savedTask = taskRepository.save(task);
        
        // Recalcular métricas do projeto automaticamente
        recalculateProjectMetrics(savedTask.getProject().getId());
        
        return convertToViewDto(savedTask);
    }

    /**
     * Obtém todas as tarefas de um projeto.
     * Verifica se o usuário pode acessar o projeto.
     * 
     * @param projectId ID do projeto
     * @return List<TaskViewDto> lista de tarefas do projeto
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByProject(Long projectId) {
        // Verificar se o usuário pode acessar o projeto
        if (!canAccessProject(projectId)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        // Verificar se o projeto existe
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtém tarefas de um projeto por status específico.
     * Verifica se o usuário pode acessar o projeto.
     * 
     * @param projectId ID do projeto
     * @param status status das tarefas a serem filtradas
     * @return List<TaskViewDto> lista de tarefas filtradas por status
     * @throws SecurityException se usuário não tiver acesso ao projeto
     */
    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        // Verificar se o usuário pode acessar o projeto
        if (!canAccessProject(projectId)) {
            throw new SecurityException("Usuário não tem acesso a este projeto");
        }
        
        // Verificar se o projeto existe
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        return taskRepository.findByProjectIdAndStatus(projectId, status).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtém uma tarefa específica por ID.
     * Verifica se o usuário pode acessar o projeto da tarefa.
     * 
     * @param taskId ID da tarefa
     * @return Optional<TaskViewDto> tarefa encontrada ou vazio se não acessível
     * @throws SecurityException se usuário não tiver acesso à tarefa
     */
    @Transactional(readOnly = true)
    public Optional<TaskViewDto> getTaskById(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findByIdWithDetails(taskId);
        
        if (taskOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Task task = taskOpt.get();
        
        // Verificar se o usuário pode acessar o projeto da tarefa
        if (!canAccessProject(task.getProject().getId())) {
            throw new SecurityException("Usuário não tem acesso a esta tarefa");
        }
        
        return Optional.of(convertToViewDto(task));
    }

    /**
     * Obtém tarefas atribuídas a um usuário específico.
     * Para USER: apenas próprias tarefas
     * Para MANAGER/ADMIN: tarefas do usuário em projetos acessíveis
     * 
     * @param userId ID do usuário
     * @return List<TaskViewDto> lista de tarefas atribuídas ao usuário
     * @throws SecurityException se USER tentar acessar tarefas de outro usuário
     */
    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByAssignedUser(Long userId) {
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        // USER só pode ver suas próprias tarefas
        if ("USER".equals(userRole) && !currentUser.getId().equals(userId)) {
            throw new SecurityException("Usuário não tem permissão para ver tarefas de outros usuários");
        }
        
        List<Task> tasks = taskRepository.findByAssignedUserId(userId);
        
        // Filtrar tarefas baseado no acesso ao projeto
        return tasks.stream()
                .filter(task -> canAccessProject(task.getProject().getId()))
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário está na equipe de um projeto da tarefa.
     * Usado principalmente pelo sistema de segurança do Spring.
     * 
     * @param taskId ID da tarefa
     * @param userId ID do usuário
     * @return boolean true se o usuário está na equipe do projeto
     */
    public boolean isUserInTaskProject(Long taskId, Long userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        Task task = taskOpt.get();
        User currentUser = getCurrentUser();
        String userRole = getCurrentUserRole();
        
        return projectRepository.isProjectAccessibleByUser(task.getProject().getId(), currentUser.getId(), userRole);
    }

    /**
     * Obtém tarefas atrasadas de um projeto.
     * 
     * Retorna tarefas que têm data de conclusão planejada anterior
     * à data atual e ainda não foram concluídas.
     * 
     * @param projectId ID do projeto
     * @return List<TaskViewDto> lista de tarefas atrasadas
     */
    @Transactional(readOnly = true)
    public List<TaskViewDto> getOverdueTasksByProject(Long projectId) {
        // Verificar se o projeto existe
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        return taskRepository.findOverdueTasksByProject(projectId, LocalDate.now()).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza uma tarefa existente.
     * 
     * Permite atualização parcial dos dados da tarefa. Valida se o novo título
     * não está em uso por outra tarefa no mesmo projeto antes de alterar.
     * 
     * @param taskId ID da tarefa a ser atualizada
     * @param taskUpdateDto dados para atualização (apenas campos não nulos são atualizados)
     * @return TaskViewDto dados atualizados da tarefa
     * @throws RuntimeException se tarefa não for encontrada, título já existir ou usuário não for encontrado
     */
    public TaskViewDto updateTask(Long taskId, TaskCreateDto taskUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + taskId + " não foi encontrada"));

        // Verificar se o título já existe no projeto (apenas se for diferente do atual)
        if (taskUpdateDto.getTitle() != null && 
            !task.getTitle().equals(taskUpdateDto.getTitle()) &&
            taskRepository.existsByProjectIdAndTitle(task.getProject().getId(), taskUpdateDto.getTitle())) {
            throw new RuntimeException("Já existe uma tarefa com este título neste projeto!");
        }

        // Atualizar campos
        if (taskUpdateDto.getTitle() != null) {
            task.setTitle(taskUpdateDto.getTitle());
        }
        if (taskUpdateDto.getDescription() != null) {
            task.setDescription(taskUpdateDto.getDescription());
        }
        if (taskUpdateDto.getStartDatePlanned() != null) {
            task.setStartDatePlanned(taskUpdateDto.getStartDatePlanned());
        }
        if (taskUpdateDto.getEndDatePlanned() != null) {
            task.setEndDatePlanned(taskUpdateDto.getEndDatePlanned());
        }
        if (taskUpdateDto.getStartDateActual() != null) {
            task.setStartDateActual(taskUpdateDto.getStartDateActual());
        }
        if (taskUpdateDto.getEndDateActual() != null) {
            task.setEndDateActual(taskUpdateDto.getEndDateActual());
        }
        if (taskUpdateDto.getProgressPercentage() != null) {
            task.updateProgress(taskUpdateDto.getProgressPercentage());
        }
        if (taskUpdateDto.getPriority() != null) {
            task.setPriority(taskUpdateDto.getPriority());
        }
        if (taskUpdateDto.getEstimatedHours() != null) {
            task.setEstimatedHours(taskUpdateDto.getEstimatedHours());
        }
        if (taskUpdateDto.getActualHours() != null) {
            task.setActualHours(taskUpdateDto.getActualHours());
        }
        if (taskUpdateDto.getNotes() != null) {
            task.setNotes(taskUpdateDto.getNotes());
        }

        // Atualizar usuário responsável
        if (taskUpdateDto.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(taskUpdateDto.getAssignedUserId())
                    .orElseThrow(() -> new UserNotFoundException("Usuário responsável com ID " + taskUpdateDto.getAssignedUserId() + " não foi encontrado"));
            task.setAssignedUser(assignedUser);
        }

        Task savedTask = taskRepository.save(task);
        
        // Recalcular métricas do projeto automaticamente
        recalculateProjectMetrics(savedTask.getProject().getId());
        
        return convertToViewDto(savedTask);
    }

    /**
     * Atualiza apenas o status de uma tarefa.
     * 
     * Método otimizado para drag-and-drop do quadro Kanban.
     * Aplica lógica automática baseada na mudança de status, como
     * definir datas de início/fim e progresso.
     * 
     * @param taskId ID da tarefa
     * @param statusUpdateDto dados para atualização do status
     * @return TaskViewDto dados atualizados da tarefa
     * @throws RuntimeException se tarefa não for encontrada
     */
    public TaskViewDto updateTaskStatus(Long taskId, TaskUpdateStatusDto statusUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + taskId + " não foi encontrada"));

        TaskStatus oldStatus = task.getStatus();
        
        // Usar o novo método que sincroniza status e progresso automaticamente
        task.updateStatus(statusUpdateDto.getStatus());

        // Adicionar notas sobre a mudança de status se fornecidas
        if (statusUpdateDto.getNotes() != null && !statusUpdateDto.getNotes().trim().isEmpty()) {
            String currentNotes = task.getNotes() != null ? task.getNotes() : "";
            String statusChangeNote = String.format("\n[%s] Status alterado de %s para %s: %s", 
                LocalDate.now(), oldStatus.getDescription(), statusUpdateDto.getStatus().getDescription(), statusUpdateDto.getNotes());
            task.setNotes(currentNotes + statusChangeNote);
        }

        Task savedTask = taskRepository.save(task);
        
        // Recalcular métricas do projeto automaticamente
        recalculateProjectMetrics(savedTask.getProject().getId());
        
        return convertToViewDto(savedTask);
    }

    /**
     * Exclui uma tarefa do sistema.
     * 
     * @param projectId ID do projeto ao qual a tarefa deve pertencer
     * @param taskId ID da tarefa a ser excluída
     * @throws TaskNotFoundException se tarefa não for encontrada ou não pertencer ao projeto
     */
    public void deleteTask(Long projectId, Long taskId) {
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + taskId + " não foi encontrada no projeto " + projectId));
        
        taskRepository.delete(task);
    }

    /**
     * Conta o número de tarefas por projeto e status.
     * 
     * @param projectId ID do projeto
     * @param status status das tarefas (pode ser null para contar todas)
     * @return Long número de tarefas encontradas
     */
    @Transactional(readOnly = true)
    public Long countTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        if (status == null) {
            return taskRepository.countByProjectId(projectId);
        }
        return taskRepository.countByProjectIdAndStatus(projectId, status);
    }

    /**
     * Obtém estatísticas das tarefas de um projeto.
     * 
     * @param projectId ID do projeto
     * @return TaskStatisticsDto estatísticas das tarefas
     */
    @Transactional(readOnly = true)
    public TaskStatisticsDto getProjectTaskStatistics(Long projectId) {
        // Verificar se o projeto existe
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));
        
        TaskStatisticsDto statistics = new TaskStatisticsDto();
        
        statistics.setTotalTasks(countTasksByProjectAndStatus(projectId, null));
        statistics.setTasksAFazer(countTasksByProjectAndStatus(projectId, TaskStatus.A_FAZER));
        statistics.setTasksEmAndamento(countTasksByProjectAndStatus(projectId, TaskStatus.EM_ANDAMENTO));
        statistics.setTasksConcluidas(countTasksByProjectAndStatus(projectId, TaskStatus.CONCLUIDA));
        statistics.setTasksBloqueadas(countTasksByProjectAndStatus(projectId, TaskStatus.BLOQUEADA));
        statistics.setTasksCanceladas(countTasksByProjectAndStatus(projectId, TaskStatus.CANCELADA));

        // Calcular percentual de conclusão
        if (statistics.getTotalTasks() > 0) {
            statistics.setCompletionPercentage(
                (statistics.getTasksConcluidas() * 100.0) / statistics.getTotalTasks()
            );
        } else {
            statistics.setCompletionPercentage(0.0);
        }

        return statistics;
    }

    /**
     * Recalcula o progresso de um projeto baseado no progresso das tarefas.
     * 
     * @param projectId ID do projeto
     */
    private void recalculateProjectProgress(Long projectId) {
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
     * Recalcula o custo realizado de um projeto baseado nos custos das tarefas.
     * 
     * @param projectId ID do projeto
     */
    private void recalculateProjectRealizedCost(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado"));

        // Calcular custo total de todas as tarefas (não apenas concluídas para mostrar custo atual)
        BigDecimal totalTaskCosts = taskRepository.findByProjectId(projectId)
                .stream()
                .map(Task::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        project.updateRealizedCost(totalTaskCosts);
        projectRepository.save(project);
    }

    /**
     * Recalcula automaticamente tanto progresso quanto custos do projeto.
     * 
     * @param projectId ID do projeto
     */
    private void recalculateProjectMetrics(Long projectId) {
        recalculateProjectProgress(projectId);
        recalculateProjectRealizedCost(projectId);
    }

    /**
     * Converte uma entidade Task para DTO de visualização.
     * 
     * @param task entidade Task a ser convertida
     * @return TaskViewDto DTO para visualização
     */
    private TaskViewDto convertToViewDto(Task task) {
        TaskViewDto dto = new TaskViewDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setStatusDescription(task.getStatus().getDescription());
        dto.setStartDatePlanned(task.getStartDatePlanned());
        dto.setEndDatePlanned(task.getEndDatePlanned());
        dto.setStartDateActual(task.getStartDateActual());
        dto.setEndDateActual(task.getEndDateActual());
        dto.setProgressPercentage(task.getProgressPercentage());
        dto.setPriority(task.getPriority());
        dto.setPriorityDescription(task.getPriorityDescription());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());
        dto.setNotes(task.getNotes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        // Dados do projeto
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        // Dados do usuário responsável
        if (task.getAssignedUser() != null) {
            dto.setAssignedUserId(task.getAssignedUser().getId());
            dto.setAssignedUserName(task.getAssignedUser().getFullName());
            dto.setAssignedUserEmail(task.getAssignedUser().getEmail());
        }

        // Dados do usuário que criou
        if (task.getCreatedByUser() != null) {
            dto.setCreatedByUserId(task.getCreatedByUser().getId());
            dto.setCreatedByUserName(task.getCreatedByUser().getFullName());
            dto.setCreatedByUserEmail(task.getCreatedByUser().getEmail());
        }

        // Flags de conveniência
        dto.setAssigned(task.isAssigned());
        dto.setCompleted(task.isCompleted());
        dto.setInProgress(task.isInProgress());
        dto.setOverdue(task.getEndDatePlanned() != null && 
                      task.getEndDatePlanned().isBefore(LocalDate.now()) && 
                      !task.isCompleted());

        return dto;
    }

    /**
     * DTO interno para estatísticas de tarefas.
     */
    public static class TaskStatisticsDto {
        private Long totalTasks;
        private Long tasksAFazer;
        private Long tasksEmAndamento;
        private Long tasksConcluidas;
        private Long tasksBloqueadas;
        private Long tasksCanceladas;
        private Double completionPercentage;

        // Getters e Setters
        public Long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(Long totalTasks) { this.totalTasks = totalTasks; }
        public Long getTasksAFazer() { return tasksAFazer; }
        public void setTasksAFazer(Long tasksAFazer) { this.tasksAFazer = tasksAFazer; }
        public Long getTasksEmAndamento() { return tasksEmAndamento; }
        public void setTasksEmAndamento(Long tasksEmAndamento) { this.tasksEmAndamento = tasksEmAndamento; }
        public Long getTasksConcluidas() { return tasksConcluidas; }
        public void setTasksConcluidas(Long tasksConcluidas) { this.tasksConcluidas = tasksConcluidas; }
        public Long getTasksBloqueadas() { return tasksBloqueadas; }
        public void setTasksBloqueadas(Long tasksBloqueadas) { this.tasksBloqueadas = tasksBloqueadas; }
        public Long getTasksCanceladas() { return tasksCanceladas; }
        public void setTasksCanceladas(Long tasksCanceladas) { this.tasksCanceladas = tasksCanceladas; }
        public Double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
    }
} 