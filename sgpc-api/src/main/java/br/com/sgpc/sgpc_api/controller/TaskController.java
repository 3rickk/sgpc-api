package br.com.sgpc.sgpc_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.sgpc.sgpc_api.dto.TaskCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskUpdateStatusDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.entity.Attachment;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.service.FileStorageService;
import br.com.sgpc.sgpc_api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller responsável pelo gerenciamento de tarefas.
 * 
 * Este controller fornece endpoints para CRUD completo de tarefas,
 * visualização em formato Kanban, controle de status, anexos e
 * estatísticas de progresso por projeto.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Gerenciamento de Tarefas", description = "Endpoints para gerenciamento de tarefas, Kanban e anexos")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria uma nova tarefa em um projeto.
     * 
     * @param projectId ID do projeto ao qual a tarefa pertence
     * @param taskCreateDto dados da tarefa a ser criada
     * @return ResponseEntity contendo os dados da tarefa criada
     * @throws RuntimeException se ocorrer erro na criação
     */
    @PostMapping
    @Operation(
        summary = "Criar nova tarefa",
        description = "Cria uma nova tarefa dentro de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TaskViewDto> createTask(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "Dados da tarefa a ser criada", required = true)
            @Valid @RequestBody TaskCreateDto taskCreateDto) {
        try {
            // Por enquanto usando um ID fixo (usuário admin), 
            // depois será implementado com autenticação completa
            Long createdByUserId = 1L;
            
            TaskViewDto createdTask = taskService.createTask(projectId, taskCreateDto, createdByUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar tarefa: " + e.getMessage());
        }
    }

    /**
     * Obtém todas as tarefas de um projeto.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo lista de tarefas do projeto
     */
    @GetMapping
    @Operation(
        summary = "Listar tarefas do projeto",
        description = "Obtém todas as tarefas de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas obtida com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<List<TaskViewDto>> getTasksByProject(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        List<TaskViewDto> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtém uma tarefa específica por ID.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @return ResponseEntity contendo os dados da tarefa ou 404 se não encontrada
     */
    @GetMapping("/{taskId}")
    @Operation(
        summary = "Obter tarefa por ID",
        description = "Obtém os detalhes de uma tarefa específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa encontrada",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TaskViewDto> getTaskById(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId, 
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Atualiza uma tarefa existente.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa a ser atualizada
     * @param taskUpdateDto dados para atualização da tarefa
     * @return ResponseEntity contendo os dados atualizados da tarefa
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PutMapping("/{taskId}")
    @Operation(
        summary = "Atualizar tarefa",
        description = "Atualiza os dados de uma tarefa existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TaskViewDto> updateTask(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Dados para atualização da tarefa", required = true)
            @Valid @RequestBody TaskCreateDto taskUpdateDto) {
        try {
            TaskViewDto updatedTask = taskService.updateTask(taskId, taskUpdateDto);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar tarefa: " + e.getMessage());
        }
    }

    /**
     * Exclui uma tarefa.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa a ser excluída
     * @return ResponseEntity sem conteúdo (204)
     * @throws RuntimeException se ocorrer erro na exclusão
     */
    @DeleteMapping("/{taskId}")
    @Operation(
        summary = "Excluir tarefa",
        description = "Remove uma tarefa do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId, 
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar tarefa: " + e.getMessage());
        }
    }

    /**
     * Atualiza apenas o status de uma tarefa.
     * 
     * Endpoint otimizado para drag-and-drop do quadro Kanban.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @param statusUpdateDto dados para atualização do status
     * @return ResponseEntity contendo os dados atualizados da tarefa
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PatchMapping("/{taskId}/status")
    @Operation(
        summary = "Atualizar status da tarefa",
        description = "Endpoint específico para atualização de status (drag-and-drop do Kanban)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TaskViewDto> updateTaskStatus(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Novo status da tarefa", required = true)
            @Valid @RequestBody TaskUpdateStatusDto statusUpdateDto) {
        try {
            TaskViewDto updatedTask = taskService.updateTaskStatus(taskId, statusUpdateDto);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar status da tarefa: " + e.getMessage());
        }
    }

    /**
     * Obtém tarefas por status específico.
     * 
     * @param projectId ID do projeto
     * @param status status das tarefas a serem filtradas
     * @return ResponseEntity contendo lista de tarefas com o status especificado
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Filtrar tarefas por status",
        description = "Obtém todas as tarefas de um projeto com status específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas filtradas"),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<TaskViewDto>> getTasksByStatus(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId, 
            @Parameter(description = "Status das tarefas (A_FAZER, EM_ANDAMENTO, CONCLUIDA, BLOQUEADA, CANCELADA)", required = true)
            @PathVariable String status) {
        try {
            TaskStatus taskStatus = TaskStatus.fromString(status);
            List<TaskViewDto> tasks = taskService.getTasksByProjectAndStatus(projectId, taskStatus);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtém dados para o quadro Kanban.
     * 
     * Retorna todas as tarefas organizadas por status para
     * exibição no formato de quadro Kanban.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo dados organizados para o Kanban
     * @throws RuntimeException se ocorrer erro ao obter os dados
     */
    @GetMapping("/kanban")
    @Operation(
        summary = "Obter quadro Kanban",
        description = "Retorna tarefas organizadas por status para exibição em quadro Kanban"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quadro Kanban obtido com sucesso",
                    content = @Content(schema = @Schema(implementation = KanbanBoardDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<KanbanBoardDto> getKanbanBoard(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        try {
            KanbanBoardDto kanbanBoard = new KanbanBoardDto();
            
            // Obter tarefas por status para o quadro Kanban
            kanbanBoard.setAFazer(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.A_FAZER));
            kanbanBoard.setEmAndamento(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.EM_ANDAMENTO));
            kanbanBoard.setConcluidas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.CONCLUIDA));
            kanbanBoard.setBloqueadas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.BLOQUEADA));
            kanbanBoard.setCanceladas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.CANCELADA));

            // Estatísticas
            kanbanBoard.setTotalTasks(
                kanbanBoard.getAFazer().size() + 
                kanbanBoard.getEmAndamento().size() + 
                kanbanBoard.getConcluidas().size() + 
                kanbanBoard.getBloqueadas().size() + 
                kanbanBoard.getCanceladas().size()
            );

            return ResponseEntity.ok(kanbanBoard);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter quadro Kanban: " + e.getMessage());
        }
    }

    /**
     * Obtém tarefas atrasadas de um projeto.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo lista de tarefas atrasadas
     */
    @GetMapping("/overdue")
    @Operation(
        summary = "Obter tarefas atrasadas",
        description = "Retorna todas as tarefas do projeto que estão com prazo vencido"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas atrasadas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<List<TaskViewDto>> getOverdueTasks(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        List<TaskViewDto> overdueTasks = taskService.getOverdueTasksByProject(projectId);
        return ResponseEntity.ok(overdueTasks);
    }

    /**
     * Obtém tarefas atribuídas a um usuário específico.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ResponseEntity contendo lista de tarefas do usuário
     */
    @GetMapping("/assigned/{userId}")
    @Operation(
        summary = "Obter tarefas por usuário",
        description = "Retorna todas as tarefas atribuídas a um usuário específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas do usuário"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<TaskViewDto>> getTasksByAssignedUser(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId, 
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId) {
        List<TaskViewDto> tasks = taskService.getTasksByAssignedUser(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtém estatísticas das tarefas do projeto.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo estatísticas das tarefas
     * @throws RuntimeException se ocorrer erro ao calcular estatísticas
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Obter estatísticas das tarefas",
        description = "Retorna estatísticas detalhadas das tarefas do projeto, incluindo contadores por status e percentual de conclusão"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskStatisticsDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        try {
            TaskStatisticsDto statistics = new TaskStatisticsDto();
            
            statistics.setTotalTasks(taskService.countTasksByProjectAndStatus(projectId, null));
            statistics.setTasksAFazer(taskService.countTasksByProjectAndStatus(projectId, TaskStatus.A_FAZER));
            statistics.setTasksEmAndamento(taskService.countTasksByProjectAndStatus(projectId, TaskStatus.EM_ANDAMENTO));
            statistics.setTasksConcluidas(taskService.countTasksByProjectAndStatus(projectId, TaskStatus.CONCLUIDA));
            statistics.setTasksBloqueadas(taskService.countTasksByProjectAndStatus(projectId, TaskStatus.BLOQUEADA));
            statistics.setTasksCanceladas(taskService.countTasksByProjectAndStatus(projectId, TaskStatus.CANCELADA));

            // Calcular percentual de conclusão
            if (statistics.getTotalTasks() > 0) {
                statistics.setCompletionPercentage(
                    (statistics.getTasksConcluidas() * 100.0) / statistics.getTotalTasks()
                );
            } else {
                statistics.setCompletionPercentage(0.0);
            }

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter estatísticas das tarefas: " + e.getMessage());
        }
    }

    // Endpoints para anexos
    
    /**
     * Faz upload de um anexo para uma tarefa.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @param file arquivo a ser anexado
     * @param authentication dados do usuário autenticado
     * @return ResponseEntity contendo dados do anexo criado
     * @throws RuntimeException se ocorrer erro no upload
     */
    @PostMapping("/{taskId}/attachments")
    @Operation(
        summary = "Upload de anexo para tarefa",
        description = "Faz upload de um arquivo como anexo de uma tarefa"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Anexo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Attachment.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "413", description = "Arquivo muito grande")
    })
    public ResponseEntity<Attachment> uploadTaskAttachment(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Arquivo a ser anexado", required = true)
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            // Verificar se a tarefa existe
            taskService.getTaskById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

            // Obter usuário autenticado
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Fazer upload do arquivo
            Attachment attachment = fileStorageService.storeFile(file, "Task", taskId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    /**
     * Obtém todos os anexos de uma tarefa.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @return ResponseEntity contendo lista de anexos da tarefa
     */
    @GetMapping("/{taskId}/attachments")
    @Operation(
        summary = "Listar anexos da tarefa",
        description = "Obtém todos os anexos de uma tarefa específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anexos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<List<Attachment>> getTaskAttachments(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId) {
        // Verificar se a tarefa existe
        taskService.getTaskById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        List<Attachment> attachments = fileStorageService.getAttachmentsByEntity("Task", taskId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Faz download de um anexo.
     * 
     * @param attachmentId ID do anexo
     * @return ResponseEntity contendo o arquivo para download
     * @throws RuntimeException se ocorrer erro no download
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(
        summary = "Download de anexo",
        description = "Faz download de um anexo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo para download"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<Resource> downloadTaskAttachment(
            @Parameter(description = "ID do anexo", required = true)
            @PathVariable Long attachmentId) {
        try {
            Attachment attachment = fileStorageService.getAttachment(attachmentId);
            Resource resource = fileStorageService.loadFileAsResource(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer download do arquivo: " + e.getMessage());
        }
    }

    /**
     * Exclui um anexo.
     * 
     * @param attachmentId ID do anexo a ser excluído
     * @return ResponseEntity sem conteúdo (204)
     * @throws RuntimeException se ocorrer erro na exclusão
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(
        summary = "Excluir anexo",
        description = "Remove um anexo do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Anexo excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<Void> deleteTaskAttachment(
            @Parameter(description = "ID do anexo", required = true)
            @PathVariable Long attachmentId) {
        try {
            fileStorageService.deleteFile(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar anexo: " + e.getMessage());
        }
    }

    /**
     * DTO para dados do quadro Kanban.
     * 
     * Representa a organização das tarefas por status para exibição
     * em formato de quadro Kanban.
     */
    @Schema(description = "Dados organizados para exibição em quadro Kanban")
    public static class KanbanBoardDto {
        
        @Schema(description = "Tarefas com status 'A Fazer'")
        private List<TaskViewDto> aFazer;
        
        @Schema(description = "Tarefas com status 'Em Andamento'")
        private List<TaskViewDto> emAndamento;
        
        @Schema(description = "Tarefas com status 'Concluída'")
        private List<TaskViewDto> concluidas;
        
        @Schema(description = "Tarefas com status 'Bloqueada'")
        private List<TaskViewDto> bloqueadas;
        
        @Schema(description = "Tarefas com status 'Cancelada'")
        private List<TaskViewDto> canceladas;
        
        @Schema(description = "Total de tarefas em todos os status")
        private int totalTasks;

        // Getters e Setters
        public List<TaskViewDto> getAFazer() { return aFazer; }
        public void setAFazer(List<TaskViewDto> aFazer) { this.aFazer = aFazer; }
        public List<TaskViewDto> getEmAndamento() { return emAndamento; }
        public void setEmAndamento(List<TaskViewDto> emAndamento) { this.emAndamento = emAndamento; }
        public List<TaskViewDto> getConcluidas() { return concluidas; }
        public void setConcluidas(List<TaskViewDto> concluidas) { this.concluidas = concluidas; }
        public List<TaskViewDto> getBloqueadas() { return bloqueadas; }
        public void setBloqueadas(List<TaskViewDto> bloqueadas) { this.bloqueadas = bloqueadas; }
        public List<TaskViewDto> getCanceladas() { return canceladas; }
        public void setCanceladas(List<TaskViewDto> canceladas) { this.canceladas = canceladas; }
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    }

    /**
     * DTO para estatísticas das tarefas.
     * 
     * Contém contadores e métricas das tarefas por status,
     * incluindo percentual de conclusão.
     */
    @Schema(description = "Estatísticas das tarefas por status e métricas de progresso")
    public static class TaskStatisticsDto {
        
        @Schema(description = "Total de tarefas no projeto")
        private Long totalTasks;
        
        @Schema(description = "Quantidade de tarefas 'A Fazer'")
        private Long tasksAFazer;
        
        @Schema(description = "Quantidade de tarefas 'Em Andamento'")
        private Long tasksEmAndamento;
        
        @Schema(description = "Quantidade de tarefas 'Concluídas'")
        private Long tasksConcluidas;
        
        @Schema(description = "Quantidade de tarefas 'Bloqueadas'")
        private Long tasksBloqueadas;
        
        @Schema(description = "Quantidade de tarefas 'Canceladas'")
        private Long tasksCanceladas;
        
        @Schema(description = "Percentual de conclusão do projeto", example = "75.5")
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