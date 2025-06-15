package br.com.sgpc.sgpc_api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.TaskCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskUpdateStatusDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.ServiceRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.TaskServiceRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskServiceRepository taskServiceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProjectService projectService;

    public TaskViewDto createTask(Long projectId, TaskCreateDto taskCreateDto, Long createdByUserId) {
        // Verificar se o projeto existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        // Verificar se já existe uma tarefa com o mesmo título no projeto
        if (taskRepository.existsByProjectIdAndTitle(projectId, taskCreateDto.getTitle())) {
            throw new RuntimeException("Já existe uma tarefa com este título neste projeto!");
        }

        // Verificar se o usuário criador existe
        User createdByUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("Usuário criador não encontrado"));

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
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));
            task.setAssignedUser(assignedUser);
        }

        Task savedTask = taskRepository.save(task);
        return convertToViewDto(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TaskViewDto> getTaskById(Long taskId) {
        return taskRepository.findByIdWithDetails(taskId)
                .map(this::convertToViewDto);
    }

    @Transactional(readOnly = true)
    public List<TaskViewDto> getTasksByAssignedUser(Long userId) {
        return taskRepository.findByAssignedUserId(userId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskViewDto> getOverdueTasksByProject(Long projectId) {
        return taskRepository.findOverdueTasksByProject(projectId, LocalDate.now()).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    public TaskViewDto updateTask(Long taskId, TaskCreateDto taskUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

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
        if (taskUpdateDto.getStatus() != null) {
            task.setStatus(taskUpdateDto.getStatus());
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
            task.setProgressPercentage(taskUpdateDto.getProgressPercentage());
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
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));
            task.setAssignedUser(assignedUser);
        }

        Task savedTask = taskRepository.save(task);
        return convertToViewDto(savedTask);
    }

    // Método específico para atualização de status (otimizado para drag-and-drop do Kanban)
    public TaskViewDto updateTaskStatus(Long taskId, TaskUpdateStatusDto statusUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(statusUpdateDto.getStatus());

        // Lógica automática baseada na mudança de status
        if (statusUpdateDto.getStatus() == TaskStatus.EM_ANDAMENTO && task.getStartDateActual() == null) {
            task.setStartDateActual(LocalDate.now());
        }
        
        if (statusUpdateDto.getStatus() == TaskStatus.CONCLUIDA) {
            task.setEndDateActual(LocalDate.now());
            task.setProgressPercentage(100);
        }

        // Adicionar notas sobre a mudança de status se fornecidas
        if (statusUpdateDto.getNotes() != null && !statusUpdateDto.getNotes().trim().isEmpty()) {
            String currentNotes = task.getNotes() != null ? task.getNotes() : "";
            String statusChangeNote = String.format("\n[%s] Status alterado de %s para %s: %s", 
                LocalDate.now(), oldStatus.getDescription(), statusUpdateDto.getStatus().getDescription(), statusUpdateDto.getNotes());
            task.setNotes(currentNotes + statusChangeNote);
        }

        Task savedTask = taskRepository.save(task);
        return convertToViewDto(savedTask);
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Long countTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        if (status == null) {
            return taskRepository.countByProjectId(projectId);
        }
        return taskRepository.countByProjectIdAndStatus(projectId, status);
    }

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
} 