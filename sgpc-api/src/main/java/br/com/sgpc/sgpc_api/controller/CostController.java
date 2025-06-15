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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cost")
public class CostController {

    @Autowired
    private CostManagementService costManagementService;

    @Autowired
    private ProjectService projectService;

    // Endpoints para gestão de serviços (RF06)
    @PostMapping("/services")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreateDto serviceCreateDto) {
        ServiceDto createdService = costManagementService.createService(serviceCreateDto);
        return ResponseEntity.ok(createdService);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDto>> getAllActiveServices() {
        List<ServiceDto> services = costManagementService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/search")
    public ResponseEntity<List<ServiceDto>> searchServices(@RequestParam String name) {
        List<ServiceDto> services = costManagementService.searchServices(name);
        return ResponseEntity.ok(services);
    }

    // Endpoints para gestão de serviços em tarefas
    @PostMapping("/tasks/{taskId}/services")
    public ResponseEntity<TaskServiceDto> addServiceToTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskServiceCreateDto serviceCreateDto) {
        TaskServiceDto taskService = costManagementService.addServiceToTask(taskId, serviceCreateDto);
        return ResponseEntity.ok(taskService);
    }

    @GetMapping("/tasks/{taskId}/services")
    public ResponseEntity<List<TaskServiceDto>> getTaskServices(@PathVariable Long taskId) {
        List<TaskServiceDto> taskServices = costManagementService.getTaskServices(taskId);
        return ResponseEntity.ok(taskServices);
    }

    @DeleteMapping("/tasks/{taskId}/services/{serviceId}")
    public ResponseEntity<Void> removeServiceFromTask(
            @PathVariable Long taskId,
            @PathVariable Long serviceId) {
        costManagementService.removeServiceFromTask(taskId, serviceId);
        return ResponseEntity.noContent().build();
    }

    // Endpoints para atualização de progresso (RF14)
    @PutMapping("/tasks/{taskId}/progress")
    public ResponseEntity<TaskViewDto> updateTaskProgress(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskProgressUpdateDto progressUpdateDto) {
        TaskViewDto updatedTask = costManagementService.updateTaskProgress(taskId, progressUpdateDto);
        return ResponseEntity.ok(updatedTask);
    }

    // Endpoints para relatórios de custos
    @GetMapping("/tasks/{taskId}/report")
    public ResponseEntity<TaskCostReportDto> getTaskCostReport(@PathVariable Long taskId) {
        TaskCostReportDto report = costManagementService.getTaskCostReport(taskId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/tasks/{taskId}/recalculate")
    public ResponseEntity<Void> recalculateTaskCosts(@PathVariable Long taskId) {
        costManagementService.recalculateTaskCosts(taskId);
        return ResponseEntity.noContent().build();
    }

    // Endpoints para gestão de orçamento de projetos (RF09)
    @GetMapping("/projects/{projectId}/budget")
    public ResponseEntity<ProjectBudgetDto> getProjectBudget(@PathVariable Long projectId) {
        ProjectBudgetDto budget = projectService.getProjectBudget(projectId);
        return ResponseEntity.ok(budget);
    }

    @PostMapping("/projects/{projectId}/recalculate-cost")
    public ResponseEntity<Void> recalculateProjectRealizedCost(@PathVariable Long projectId) {
        projectService.recalculateProjectRealizedCost(projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{projectId}/recalculate-progress")
    public ResponseEntity<Void> recalculateProjectProgress(@PathVariable Long projectId) {
        projectService.recalculateProjectProgress(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/budget-report")
    public ResponseEntity<List<ProjectBudgetDto>> getAllProjectsBudgetReport() {
        List<ProjectBudgetDto> report = projectService.getAllProjectsBudgetReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/projects/over-budget")
    public ResponseEntity<List<ProjectBudgetDto>> getProjectsOverBudget() {
        List<ProjectBudgetDto> overBudgetProjects = projectService.getAllProjectsBudgetReport()
                .stream()
                .filter(project -> project.getIsOverBudget())
                .toList();
        return ResponseEntity.ok(overBudgetProjects);
    }
} 