package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.ServiceCreateDto;
import br.com.sgpc.sgpc_api.dto.ServiceDto;
import br.com.sgpc.sgpc_api.dto.TaskCostReportDto;
import br.com.sgpc.sgpc_api.dto.TaskProgressUpdateDto;
import br.com.sgpc.sgpc_api.dto.TaskServiceCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskServiceDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.TaskService;
import br.com.sgpc.sgpc_api.repository.ServiceRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.TaskServiceRepository;

@Service
@Transactional
public class CostManagementService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TaskServiceRepository taskServiceRepository;

    @Autowired
    private ProjectService projectService;

    // Gestão de Serviços (RF06)
    public ServiceDto createService(ServiceCreateDto serviceCreateDto) {
        if (serviceRepository.existsByName(serviceCreateDto.getName())) {
            throw new RuntimeException("Já existe um serviço com este nome!");
        }

        br.com.sgpc.sgpc_api.entity.Service service = new br.com.sgpc.sgpc_api.entity.Service();
        service.setName(serviceCreateDto.getName());
        service.setDescription(serviceCreateDto.getDescription());
        service.setUnitOfMeasurement(serviceCreateDto.getUnitOfMeasurement());
        service.setUnitLaborCost(serviceCreateDto.getUnitLaborCost());
        service.setUnitMaterialCost(serviceCreateDto.getUnitMaterialCost());
        service.setUnitEquipmentCost(serviceCreateDto.getUnitEquipmentCost());
        service.setIsActive(serviceCreateDto.getIsActive());

        br.com.sgpc.sgpc_api.entity.Service savedService = serviceRepository.save(service);
        return convertServiceToDto(savedService);
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getAllActiveServices() {
        return serviceRepository.findActiveServicesOrderByName().stream()
                .map(this::convertServiceToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> searchServices(String name) {
        return serviceRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertServiceToDto)
                .collect(Collectors.toList());
    }

    // Gestão de Serviços em Tarefas
    public TaskServiceDto addServiceToTask(Long taskId, TaskServiceCreateDto serviceCreateDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        br.com.sgpc.sgpc_api.entity.Service service = serviceRepository.findById(serviceCreateDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        if (taskServiceRepository.existsByTaskIdAndServiceId(taskId, serviceCreateDto.getServiceId())) {
            throw new RuntimeException("Serviço já foi adicionado a esta tarefa");
        }

        TaskService taskService = new TaskService();
        taskService.setTask(task);
        taskService.setService(service);
        taskService.setQuantity(serviceCreateDto.getQuantity());
        taskService.setUnitCostOverride(serviceCreateDto.getUnitCostOverride());
        taskService.setNotes(serviceCreateDto.getNotes());

        TaskService savedTaskService = taskServiceRepository.save(taskService);

        // Recalcular custos da tarefa
        recalculateTaskCosts(taskId);

        return convertTaskServiceToDto(savedTaskService);
    }

    public void removeServiceFromTask(Long taskId, Long serviceId) {
        if (!taskServiceRepository.existsByTaskIdAndServiceId(taskId, serviceId)) {
            throw new RuntimeException("Serviço não encontrado nesta tarefa");
        }

        taskServiceRepository.deleteByTaskIdAndServiceId(taskId, serviceId);
        recalculateTaskCosts(taskId);
    }

    @Transactional(readOnly = true)
    public List<TaskServiceDto> getTaskServices(Long taskId) {
        return taskServiceRepository.findByTaskIdWithServiceDetails(taskId).stream()
                .map(this::convertTaskServiceToDto)
                .collect(Collectors.toList());
    }

    // Atualização de Progresso (RF14)
    public TaskViewDto updateTaskProgress(Long taskId, TaskProgressUpdateDto progressUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        task.updateProgress(progressUpdateDto.getProgressPercentage());

        if (progressUpdateDto.getActualHours() != null) {
            task.setActualHours(progressUpdateDto.getActualHours());
        }

        if (progressUpdateDto.getNotes() != null && !progressUpdateDto.getNotes().trim().isEmpty()) {
            String currentNotes = task.getNotes() != null ? task.getNotes() : "";
            String progressNote = String.format("\n[%s] Progresso atualizado para %d%%: %s",
                    LocalDate.now(), progressUpdateDto.getProgressPercentage(), progressUpdateDto.getNotes());
            task.setNotes(currentNotes + progressNote);
        }

        Task savedTask = taskRepository.save(task);

        // Recalcular progresso do projeto
        projectService.recalculateProjectProgress(task.getProject().getId());

        return convertTaskToViewDto(savedTask);
    }

    // Cálculo de custos
    public void recalculateTaskCosts(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        BigDecimal laborCost = taskServiceRepository.calculateLaborCostByTaskId(taskId);
        BigDecimal materialCost = taskServiceRepository.calculateMaterialCostByTaskId(taskId);
        BigDecimal equipmentCost = taskServiceRepository.calculateEquipmentCostByTaskId(taskId);

        task.updateCosts(laborCost, materialCost, equipmentCost);
        taskRepository.save(task);

        // Recalcular custos realizados do projeto
        projectService.recalculateProjectRealizedCost(task.getProject().getId());
    }

    @Transactional(readOnly = true)
    public TaskCostReportDto getTaskCostReport(Long taskId) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        List<TaskServiceDto> services = getTaskServices(taskId);

        TaskCostReportDto report = new TaskCostReportDto();
        report.setTaskId(taskId);
        report.setTaskTitle(task.getTitle());
        report.setLaborCost(task.getLaborCost());
        report.setMaterialCost(task.getMaterialCost());
        report.setEquipmentCost(task.getEquipmentCost());
        report.setTotalCost(task.getTotalCost());
        report.setProgressPercentage(task.getProgressPercentage());
        report.setServices(services);

        return report;
    }

    // Conversores para DTOs
    private ServiceDto convertServiceToDto(br.com.sgpc.sgpc_api.entity.Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setUnitOfMeasurement(service.getUnitOfMeasurement());
        dto.setUnitLaborCost(service.getUnitLaborCost());
        dto.setUnitMaterialCost(service.getUnitMaterialCost());
        dto.setUnitEquipmentCost(service.getUnitEquipmentCost());
        dto.setTotalUnitCost(service.getTotalUnitCost());
        dto.setIsActive(service.getIsActive());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    private TaskServiceDto convertTaskServiceToDto(TaskService taskService) {
        TaskServiceDto dto = new TaskServiceDto();
        dto.setId(taskService.getId());
        dto.setTaskId(taskService.getTask().getId());
        dto.setTaskTitle(taskService.getTask().getTitle());
        dto.setServiceId(taskService.getService().getId());
        dto.setServiceName(taskService.getService().getName());
        dto.setServiceDescription(taskService.getService().getDescription());
        dto.setUnitOfMeasurement(taskService.getService().getUnitOfMeasurement());
        dto.setQuantity(taskService.getQuantity());
        dto.setUnitCostOverride(taskService.getUnitCostOverride());
        dto.setUnitLaborCost(taskService.getEffectiveUnitLaborCost());
        dto.setUnitMaterialCost(taskService.getEffectiveUnitMaterialCost());
        dto.setUnitEquipmentCost(taskService.getEffectiveUnitEquipmentCost());
        dto.setTotalLaborCost(taskService.getTotalLaborCost());
        dto.setTotalMaterialCost(taskService.getTotalMaterialCost());
        dto.setTotalEquipmentCost(taskService.getTotalEquipmentCost());
        dto.setTotalCost(taskService.getTotalCost());
        dto.setNotes(taskService.getNotes());
        dto.setCreatedAt(taskService.getCreatedAt());
        dto.setUpdatedAt(taskService.getUpdatedAt());
        return dto;
    }

    private TaskViewDto convertTaskToViewDto(Task task) {
        TaskViewDto dto = new TaskViewDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setStartDatePlanned(task.getStartDatePlanned());
        dto.setEndDatePlanned(task.getEndDatePlanned());
        dto.setStartDateActual(task.getStartDateActual());
        dto.setEndDateActual(task.getEndDateActual());
        dto.setProgressPercentage(task.getProgressPercentage());
        dto.setPriority(task.getPriority());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());
        dto.setNotes(task.getNotes());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
        if (task.getAssignedUser() != null) {
            dto.setAssignedUserId(task.getAssignedUser().getId());
            dto.setAssignedUserName(task.getAssignedUser().getFullName());
        }
        dto.setCreatedByUserId(task.getCreatedByUser().getId());
        dto.setCreatedByUserName(task.getCreatedByUser().getFullName());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
} 