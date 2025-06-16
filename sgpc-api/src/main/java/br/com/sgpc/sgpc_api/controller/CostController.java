package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.ProjectBudgetDto;
import br.com.sgpc.sgpc_api.dto.ServiceCreateDto;
import br.com.sgpc.sgpc_api.dto.ServiceDto;
import br.com.sgpc.sgpc_api.dto.TaskCostReportDto;
import br.com.sgpc.sgpc_api.dto.TaskProgressUpdateDto;
import br.com.sgpc.sgpc_api.dto.TaskServiceCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskServiceDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.service.CostManagementService;
import br.com.sgpc.sgpc_api.service.ProjectService;
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
 * Controller responsável pelo gerenciamento de custos e serviços.
 * 
 * Este controller fornece endpoints para gestão completa de serviços,
 * controle de custos de tarefas, atualização de progresso e
 * gerenciamento de orçamento de projetos.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/cost")
@Tag(name = "Gerenciamento de Custos", description = "Endpoints para gestão de custos, serviços e orçamentos")
@SecurityRequirement(name = "bearerAuth")
public class CostController {

    @Autowired
    private CostManagementService costManagementService;

    @Autowired
    private ProjectService projectService;

    /**
     * Cria um novo serviço no sistema.
     * 
     * @param serviceCreateDto dados do serviço a ser criado
     * @return ServiceDto dados do serviço criado
     */
    @Operation(
        summary = "Criar serviço",
        description = "Cria um novo serviço com custos unitários de mão de obra, materiais e equipamentos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço criado com sucesso",
                content = @Content(schema = @Schema(implementation = ServiceDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou serviço já existe"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping("/services")
    public ResponseEntity<ServiceDto> createService(
            @Valid @RequestBody @Parameter(description = "Dados do serviço") ServiceCreateDto serviceCreateDto) {
        ServiceDto createdService = costManagementService.createService(serviceCreateDto);
        return ResponseEntity.ok(createdService);
    }

    /**
     * Lista todos os serviços ativos.
     * 
     * @return List<ServiceDto> lista de serviços ativos
     */
    @Operation(
        summary = "Listar serviços ativos",
        description = "Retorna lista de todos os serviços ativos ordenados por nome"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/services")
    public ResponseEntity<List<ServiceDto>> getAllActiveServices() {
        List<ServiceDto> services = costManagementService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }

    /**
     * Busca serviços por nome.
     * 
     * @param name termo de busca para o nome do serviço
     * @return List<ServiceDto> lista de serviços encontrados
     */
    @Operation(
        summary = "Buscar serviços",
        description = "Busca serviços por nome usando correspondência parcial (case insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/services/search")
    public ResponseEntity<List<ServiceDto>> searchServices(
            @RequestParam @Parameter(description = "Nome do serviço para busca", example = "Alvenaria") String name) {
        List<ServiceDto> services = costManagementService.searchServices(name);
        return ResponseEntity.ok(services);
    }

    /**
     * Adiciona um serviço a uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @param serviceCreateDto dados do serviço para a tarefa
     * @return TaskServiceDto serviço adicionado à tarefa
     */
    @Operation(
        summary = "Adicionar serviço à tarefa",
        description = "Vincula um serviço existente a uma tarefa específica com quantidade e custos personalizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço adicionado à tarefa com sucesso",
                content = @Content(schema = @Schema(implementation = TaskServiceDto.class))),
        @ApiResponse(responseCode = "400", description = "Tarefa não encontrada ou dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PostMapping("/tasks/{taskId}/services")
    public ResponseEntity<TaskServiceDto> addServiceToTask(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId,
            @Valid @RequestBody @Parameter(description = "Dados do serviço para a tarefa") TaskServiceCreateDto serviceCreateDto) {
        TaskServiceDto taskService = costManagementService.addServiceToTask(taskId, serviceCreateDto);
        return ResponseEntity.ok(taskService);
    }

    /**
     * Lista todos os serviços de uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @return List<TaskServiceDto> lista de serviços da tarefa
     */
    @Operation(
        summary = "Listar serviços da tarefa",
        description = "Retorna todos os serviços vinculados a uma tarefa específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de serviços da tarefa retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @GetMapping("/tasks/{taskId}/services")
    public ResponseEntity<List<TaskServiceDto>> getTaskServices(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId) {
        List<TaskServiceDto> taskServices = costManagementService.getTaskServices(taskId);
        return ResponseEntity.ok(taskServices);
    }

    /**
     * Remove um serviço de uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @param serviceId ID do serviço
     */
    @Operation(
        summary = "Remover serviço da tarefa",
        description = "Remove a vinculação de um serviço específico de uma tarefa"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Serviço removido da tarefa com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa ou serviço não encontrado")
    })
    @DeleteMapping("/tasks/{taskId}/services/{serviceId}")
    public ResponseEntity<Void> removeServiceFromTask(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId,
            @PathVariable @Parameter(description = "ID do serviço", example = "1") Long serviceId) {
        costManagementService.removeServiceFromTask(taskId, serviceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza o progresso de uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @param progressUpdateDto dados de atualização do progresso
     * @return TaskViewDto tarefa atualizada
     */
    @Operation(
        summary = "Atualizar progresso da tarefa",
        description = "Atualiza o percentual de progresso de uma tarefa e recalcula custos realizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = TaskViewDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou progresso inválido"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PutMapping("/tasks/{taskId}/progress")
    public ResponseEntity<TaskViewDto> updateTaskProgress(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId,
            @Valid @RequestBody @Parameter(description = "Dados de atualização do progresso") TaskProgressUpdateDto progressUpdateDto) {
        TaskViewDto updatedTask = costManagementService.updateTaskProgress(taskId, progressUpdateDto);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Gera relatório de custos de uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @return TaskCostReportDto relatório de custos da tarefa
     */
    @Operation(
        summary = "Relatório de custos da tarefa",
        description = "Gera relatório detalhado dos custos de uma tarefa incluindo mão de obra, materiais e equipamentos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                content = @Content(schema = @Schema(implementation = TaskCostReportDto.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @GetMapping("/tasks/{taskId}/report")
    public ResponseEntity<TaskCostReportDto> getTaskCostReport(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId) {
        TaskCostReportDto report = costManagementService.getTaskCostReport(taskId);
        return ResponseEntity.ok(report);
    }

    /**
     * Recalcula custos de uma tarefa.
     * 
     * @param taskId ID da tarefa
     */
    @Operation(
        summary = "Recalcular custos da tarefa",
        description = "Força o recálculo de todos os custos de uma tarefa baseado nos serviços vinculados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Custos recalculados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PostMapping("/tasks/{taskId}/recalculate")
    public ResponseEntity<Void> recalculateTaskCosts(
            @PathVariable @Parameter(description = "ID da tarefa", example = "1") Long taskId) {
        costManagementService.recalculateTaskCosts(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtém orçamento de um projeto.
     * 
     * @param projectId ID do projeto
     * @return ProjectBudgetDto dados do orçamento do projeto
     */
    @Operation(
        summary = "Obter orçamento do projeto",
        description = "Retorna informações detalhadas do orçamento de um projeto incluindo custos estimados e realizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orçamento obtido com sucesso",
                content = @Content(schema = @Schema(implementation = ProjectBudgetDto.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @GetMapping("/projects/{projectId}/budget")
    public ResponseEntity<ProjectBudgetDto> getProjectBudget(
            @PathVariable @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        ProjectBudgetDto budget = projectService.getProjectBudget(projectId);
        return ResponseEntity.ok(budget);
    }

    /**
     * Recalcula custo realizado de um projeto.
     * 
     * @param projectId ID do projeto
     */
    @Operation(
        summary = "Recalcular custo realizado do projeto",
        description = "Força o recálculo do custo realizado do projeto baseado no progresso das tarefas"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Custo realizado recalculado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @PostMapping("/projects/{projectId}/recalculate-cost")
    public ResponseEntity<Void> recalculateProjectRealizedCost(
            @PathVariable @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        projectService.recalculateProjectRealizedCost(projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recalcula progresso de um projeto.
     * 
     * @param projectId ID do projeto
     */
    @Operation(
        summary = "Recalcular progresso do projeto",
        description = "Força o recálculo do percentual de progresso do projeto baseado nas tarefas concluídas"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Progresso recalculado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @PostMapping("/projects/{projectId}/recalculate-progress")
    public ResponseEntity<Void> recalculateProjectProgress(
            @PathVariable @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        projectService.recalculateProjectProgress(projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gera relatório de orçamento de todos os projetos.
     * 
     * @return List<ProjectBudgetDto> relatório de orçamento de todos os projetos
     */
    @Operation(
        summary = "Relatório de orçamento de todos os projetos",
        description = "Gera relatório consolidado do orçamento de todos os projetos do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/projects/budget-report")
    public ResponseEntity<List<ProjectBudgetDto>> getAllProjectsBudgetReport() {
        List<ProjectBudgetDto> report = projectService.getAllProjectsBudgetReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Lista projetos com orçamento estourado.
     * 
     * @return List<ProjectBudgetDto> lista de projetos com orçamento estourado
     */
    @Operation(
        summary = "Projetos com orçamento estourado",
        description = "Retorna lista de projetos que estão com custos acima do orçamento planejado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos com orçamento estourado retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/projects/over-budget")
    public ResponseEntity<List<ProjectBudgetDto>> getProjectsOverBudget() {
        List<ProjectBudgetDto> overBudgetProjects = projectService.getAllProjectsBudgetReport()
                .stream()
                .filter(project -> project.getIsOverBudget())
                .toList();
        return ResponseEntity.ok(overBudgetProjects);
    }
} 