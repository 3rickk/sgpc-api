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
import org.springframework.security.access.prepost.PreAuthorize;
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

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.TaskCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskUpdateStatusDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.entity.Attachment;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.exception.InvalidTaskStatusException;
import br.com.sgpc.sgpc_api.exception.TaskNotFoundException;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.service.FileStorageService;
import br.com.sgpc.sgpc_api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
     * ADMIN e MANAGER podem criar tarefas em qualquer projeto.
     * USER pode criar tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto ao qual a tarefa pertence
     * @param taskCreateDto dados da tarefa a ser criada
     * @return ResponseEntity contendo os dados da tarefa criada
     * @throws RuntimeException se ocorrer erro na criação
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @projectService.isUserInProjectTeam(#projectId, authentication.principal.id))")
    @Operation(
        summary = "Criar nova tarefa",
        description = "Cria uma nova tarefa dentro de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"Título da tarefa é obrigatório e deve ter entre 1 e 255 caracteres\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para criar tarefas neste projeto\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Tarefa já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Conflito\",\"mensagem\":\"Já existe uma tarefa com este título neste projeto\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
     * Todos os usuários autenticados podem visualizar tarefas.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo lista de tarefas do projeto
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Listar tarefas do projeto",
        description = "Obtém todas as tarefas de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas obtida com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar tarefas\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<TaskViewDto>> getTasksByProject(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        List<TaskViewDto> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtém uma tarefa específica por ID.
     * Todos os usuários autenticados podem visualizar tarefas.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @return ResponseEntity contendo os dados da tarefa ou 404 se não encontrada
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Obter tarefa por ID",
        description = "Obtém os detalhes de uma tarefa específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa encontrada",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar tarefas\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
     * ADMIN e MANAGER podem atualizar qualquer tarefa.
     * USER pode atualizar tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa a ser atualizada
     * @param taskUpdateDto dados para atualização da tarefa
     * @return ResponseEntity contendo os dados atualizados da tarefa
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @projectService.isUserInProjectTeam(#projectId, authentication.principal.id))")
    @Operation(
        summary = "Atualizar tarefa",
        description = "Atualiza os dados de uma tarefa existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"Título da tarefa é obrigatório e deve ter entre 1 e 255 caracteres\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para atualizar esta tarefa\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Título já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Conflito\",\"mensagem\":\"Já existe outra tarefa com este título neste projeto\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
     * ADMIN e MANAGER podem excluir qualquer tarefa.
     * USER pode excluir tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa a ser excluída
     * @return ResponseEntity vazio com status 204
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
        summary = "Excluir tarefa",
        description = "Remove uma tarefa do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para excluir tarefas\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId, 
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId) {
            taskService.deleteTask(projectId, taskId);
            return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza apenas o status de uma tarefa.
     * 
     * Endpoint otimizado para drag-and-drop do quadro Kanban.
     * ADMIN e MANAGER podem atualizar qualquer tarefa.
     * USER pode atualizar tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @param statusUpdateDto dados para atualização do status
     * @return ResponseEntity contendo os dados atualizados da tarefa
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @projectService.isUserInProjectTeam(#projectId, authentication.principal.id))")
    @Operation(
        summary = "Atualizar status da tarefa",
        description = "Endpoint específico para atualização de status (drag-and-drop do Kanban)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Status de tarefa inválido\",\"mensagem\":\"Status INVALID não é válido. Use: A_FAZER, EM_ANDAMENTO, CONCLUIDA, BLOQUEADA, CANCELADA\",\"path\":\"/api/projects/1/tasks/1/status\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1/status\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para alterar status de tarefas\",\"path\":\"/api/projects/1/tasks/1/status\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999/status\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1/status\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
     * Filtra tarefas por status.
     * ADMIN e MANAGER podem visualizar tarefas com qualquer status.
     * USER pode visualizar tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param status status das tarefas
     * @return ResponseEntity com lista de tarefas filtradas
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Filtrar tarefas por status",
        description = "Obtém todas as tarefas de um projeto com status específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas filtradas"),
        @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Status de tarefa inválido\",\"mensagem\":\"Status INVALID não é válido. Use: A_FAZER, EM_ANDAMENTO, CONCLUIDA, BLOQUEADA, CANCELADA\",\"path\":\"/api/projects/1/tasks/status/INVALID\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar tarefas\",\"path\":\"/api/projects/1/tasks/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
            throw new InvalidTaskStatusException("Status '" + status + "' não é válido. Use: A_FAZER, EM_ANDAMENTO, CONCLUIDA, BLOQUEADA, CANCELADA");
        }
    }

    /**
     * Obtém dados para o quadro Kanban.
     * ADMIN e MANAGER podem visualizar o quadro Kanban.
     * USER pode visualizar o quadro Kanban apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity contendo dados organizados para o Kanban
     */
    @GetMapping("/kanban")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Obter quadro Kanban",
        description = "Retorna tarefas organizadas por status para exibição em quadro Kanban"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quadro Kanban obtido com sucesso",
                    content = @Content(schema = @Schema(implementation = KanbanBoardDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/kanban\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar Kanban\",\"path\":\"/api/projects/1/tasks/kanban\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks/kanban\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/kanban\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<KanbanBoardDto> getKanbanBoard(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
            KanbanBoardDto kanbanBoard = new KanbanBoardDto();
            
            kanbanBoard.setAFazer(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.A_FAZER));
            kanbanBoard.setEmAndamento(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.EM_ANDAMENTO));
            kanbanBoard.setConcluidas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.CONCLUIDA));
            kanbanBoard.setBloqueadas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.BLOQUEADA));
            kanbanBoard.setCanceladas(taskService.getTasksByProjectAndStatus(projectId, TaskStatus.CANCELADA));

        int totalTasks = kanbanBoard.getAFazer().size() + kanbanBoard.getEmAndamento().size() + 
                        kanbanBoard.getConcluidas().size() + kanbanBoard.getBloqueadas().size() + 
                        kanbanBoard.getCanceladas().size();
        kanbanBoard.setTotalTasks(totalTasks);

            return ResponseEntity.ok(kanbanBoard);
    }

    /**
     * Obtém tarefas atrasadas do projeto.
     * ADMIN e MANAGER podem visualizar tarefas atrasadas.
     * USER pode visualizar tarefas atrasadas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity com lista de tarefas em atraso
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
        summary = "Obter tarefas atrasadas",
        description = "Retorna todas as tarefas do projeto que estão com prazo vencido"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas atrasadas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/overdue\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar tarefas atrasadas\",\"path\":\"/api/projects/1/tasks/overdue\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks/overdue\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/overdue\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<TaskViewDto>> getOverdueTasks(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        List<TaskViewDto> overdueTasks = taskService.getOverdueTasksByProject(projectId);
        return ResponseEntity.ok(overdueTasks);
    }

    /**
     * Obtém estatísticas das tarefas do projeto.
     * ADMIN e MANAGER podem visualizar estatísticas.
     * USER pode visualizar estatísticas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity com estatísticas das tarefas
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Obter estatísticas das tarefas",
        description = "Retorna estatísticas detalhadas das tarefas do projeto, incluindo contadores por status e percentual de conclusão"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskService.TaskStatisticsDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/statistics\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar estatísticas\",\"path\":\"/api/projects/1/tasks/statistics\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/tasks/statistics\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/statistics\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<TaskService.TaskStatisticsDto> getTaskStatistics(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId) {
        TaskService.TaskStatisticsDto statistics = taskService.getProjectTaskStatistics(projectId);
            return ResponseEntity.ok(statistics);
    }

    // Endpoints para anexos
    
    /**
     * Faz upload de um anexo para uma tarefa.
     * ADMIN e MANAGER podem adicionar anexos a qualquer tarefa.
     * USER pode adicionar anexos apenas em tarefas de projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @param file arquivo a ser anexado
     * @param authentication dados do usuário autenticado
     * @return ResponseEntity contendo dados do anexo criado
     */
    @PostMapping("/{taskId}/attachments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @projectService.isUserInProjectTeam(#projectId, authentication.principal.id))")
    @Operation(
        summary = "Upload de anexo para tarefa",
        description = "Faz upload de um arquivo como anexo de uma tarefa"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Anexo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Attachment.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Arquivo inválido\",\"mensagem\":\"Arquivo não pode estar vazio ou exceder 10MB\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para adicionar anexos nesta tarefa\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "413", description = "Arquivo muito grande",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":413,\"erro\":\"Arquivo muito grande\",\"mensagem\":\"Arquivo excede o tamanho máximo permitido de 10MB\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "415", description = "Tipo de arquivo não suportado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":415,\"erro\":\"Tipo não suportado\",\"mensagem\":\"Tipo de arquivo não permitido. Use: PDF, DOC, DOCX, XLS, XLSX, IMG\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
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
                    .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + taskId + " não foi encontrada"));

            // Obter usuário autenticado (por enquanto usando admin)
            Long userId = 1L; // Será substituído quando autenticação estiver completa
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Fazer upload do arquivo
            Attachment attachment = fileStorageService.storeFile(file, "Task", taskId, user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    /**
     * Lista anexos de uma tarefa.
     * ADMIN e MANAGER podem visualizar anexos de qualquer tarefa.
     * USER pode visualizar anexos apenas em tarefas de projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param taskId ID da tarefa
     * @return ResponseEntity com lista de anexos
     */
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Listar anexos da tarefa",
        description = "Obtém todos os anexos de uma tarefa específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anexos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar anexos\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Tarefa não encontrada\",\"mensagem\":\"Tarefa com ID 999 não foi encontrada\",\"path\":\"/api/projects/1/tasks/999/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<Attachment>> getTaskAttachments(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID da tarefa", required = true)
            @PathVariable Long taskId) {
        // Verificar se a tarefa existe
        if (taskService.getTaskById(taskId).isEmpty()) {
            throw new TaskNotFoundException("Tarefa com ID " + taskId + " não foi encontrada");
        }

        List<Attachment> attachments = fileStorageService.getAttachmentsByEntity("Task", taskId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Faz download de um anexo.
     * ADMIN e MANAGER podem baixar qualquer anexo.
     * USER pode baixar anexos apenas em projetos onde é membro da equipe.
     * 
     * @param attachmentId ID do anexo
     * @return ResponseEntity com arquivo para download
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Download de anexo",
        description = "Faz download de um anexo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo para download"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para baixar anexos\",\"path\":\"/api/projects/1/tasks/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Anexo não encontrado\",\"mensagem\":\"Anexo com ID 999 não foi encontrado\",\"path\":\"/api/projects/1/tasks/attachments/999/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Resource> downloadTaskAttachment(
            @Parameter(description = "ID do anexo", required = true)
            @PathVariable Long attachmentId) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(attachmentId);
            Attachment attachment = fileStorageService.getAttachment(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer download do arquivo: " + e.getMessage());
        }
    }

    /**
     * Exclui um anexo.
     * ADMIN e MANAGER podem excluir qualquer anexo.
     * USER pode excluir anexos apenas em projetos onde é membro da equipe.
     * 
     * @param attachmentId ID do anexo a ser excluído
     * @return ResponseEntity vazio com status 204
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
        summary = "Excluir anexo",
        description = "Remove um anexo do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Anexo excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para excluir anexos\",\"path\":\"/api/projects/1/tasks/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Anexo não encontrado\",\"mensagem\":\"Anexo com ID 999 não foi encontrado\",\"path\":\"/api/projects/1/tasks/attachments/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteTaskAttachment(
            @Parameter(description = "ID do anexo", required = true)
            @PathVariable Long attachmentId) {
        try {
            fileStorageService.deleteFile(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
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
     * Obtém tarefas atribuídas a um usuário específico.
     * ADMIN e MANAGER podem visualizar tarefas de qualquer usuário.
     * USER pode visualizar tarefas apenas em projetos onde é membro da equipe.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário
     * @return ResponseEntity contendo lista de tarefas atribuídas ao usuário
     */
    @GetMapping("/assigned/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and #userId == authentication.principal.id)")
    @Operation(
        summary = "Obter tarefas por usuário",
        description = "Obtém todas as tarefas atribuídas a um usuário específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tarefas obtida com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "ID de usuário inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"ID do usuário deve ser um número positivo\",\"path\":\"/api/projects/1/tasks/assigned/abc\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/tasks/assigned/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar tarefas de outros usuários\",\"path\":\"/api/projects/1/tasks/assigned/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/projects/1/tasks/assigned/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/tasks/assigned/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<TaskViewDto>> getTasksByAssignedUser(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId) {
        List<TaskViewDto> tasks = taskService.getTasksByAssignedUserAndProject(projectId, userId);
        return ResponseEntity.ok(tasks);
    }
} 