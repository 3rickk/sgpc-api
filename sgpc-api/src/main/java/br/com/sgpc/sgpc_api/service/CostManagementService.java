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
import br.com.sgpc.sgpc_api.exception.ServiceAlreadyAssignedException;
import br.com.sgpc.sgpc_api.exception.ServiceAlreadyExistsException;
import br.com.sgpc.sgpc_api.exception.ServiceNotAssignedToTaskException;
import br.com.sgpc.sgpc_api.exception.ServiceNotFoundException;
import br.com.sgpc.sgpc_api.exception.TaskNotFoundException;
import br.com.sgpc.sgpc_api.repository.ServiceRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;
import br.com.sgpc.sgpc_api.repository.TaskServiceRepository;

/**
 * Serviço responsável pelo gerenciamento de custos e serviços no sistema.
 * 
 * Esta classe implementa as funcionalidades relacionadas à gestão de serviços,
 * controle de custos de tarefas, atualização de progresso e geração de relatórios
 * de custos. Integra-se com o sistema de projetos para manter a consistência
 * dos cálculos de custos e progresso.
 * 
 * Principais funcionalidades:
 * - Gestão de serviços (CRUD)
 * - Vinculação de serviços a tarefas
 * - Cálculo automático de custos
 * - Atualização de progresso de tarefas
 * - Geração de relatórios de custos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
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

    /**
     * Cria um novo serviço no sistema.
     * 
     * Valida se não existe outro serviço com o mesmo nome antes de criar.
     * O serviço criado inclui custos unitários de mão de obra, materiais
     * e equipamentos que serão utilizados no cálculo de custos das tarefas.
     * 
     * @param serviceCreateDto dados do serviço a ser criado
     * @return ServiceDto dados do serviço criado
     * @throws ServiceAlreadyExistsException se já existir um serviço com o mesmo nome
     */
    public ServiceDto createService(ServiceCreateDto serviceCreateDto) {
        if (serviceRepository.existsByName(serviceCreateDto.getName())) {
            throw new ServiceAlreadyExistsException("Já existe um serviço cadastrado com este nome");
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

    /**
     * Lista todos os serviços ativos ordenados por nome.
     * 
     * @return List<ServiceDto> lista de serviços ativos
     */
    @Transactional(readOnly = true)
    public List<ServiceDto> getAllActiveServices() {
        return serviceRepository.findActiveServicesOrderByName().stream()
                .map(this::convertServiceToDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca serviços por nome usando correspondência parcial.
     * 
     * @param name termo de busca para o nome do serviço
     * @return List<ServiceDto> lista de serviços encontrados
     */
    @Transactional(readOnly = true)
    public List<ServiceDto> searchServices(String name) {
        return serviceRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertServiceToDto)
                .collect(Collectors.toList());
    }

    /**
     * Adiciona um serviço a uma tarefa específica.
     * 
     * Vincula um serviço existente a uma tarefa com quantidade específica
     * e opcionalmente um custo unitário personalizado. Após a vinculação,
     * recalcula automaticamente os custos da tarefa.
     * 
     * @param taskId ID da tarefa
     * @param serviceCreateDto dados do serviço para vinculação
     * @return TaskServiceDto dados do serviço vinculado à tarefa
     * @throws TaskNotFoundException se a tarefa não for encontrada
     * @throws ServiceNotFoundException se o serviço não for encontrado
     * @throws ServiceAlreadyAssignedException se o serviço já estiver vinculado
     */
    public TaskServiceDto addServiceToTask(Long taskId, TaskServiceCreateDto serviceCreateDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa não encontrada"));

        br.com.sgpc.sgpc_api.entity.Service service = serviceRepository.findById(serviceCreateDto.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado"));

        if (taskServiceRepository.existsByTaskIdAndServiceId(taskId, serviceCreateDto.getServiceId())) {
            throw new ServiceAlreadyAssignedException("Este serviço já foi adicionado a esta tarefa");
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

    /**
     * Remove um serviço de uma tarefa.
     * 
     * Remove a vinculação entre um serviço e uma tarefa e recalcula
     * automaticamente os custos da tarefa após a remoção.
     * 
     * @param taskId ID da tarefa
     * @param serviceId ID do serviço
     * @throws ServiceNotAssignedToTaskException se a vinculação não existir
     */
    public void removeServiceFromTask(Long taskId, Long serviceId) {
        if (!taskServiceRepository.existsByTaskIdAndServiceId(taskId, serviceId)) {
            throw new ServiceNotAssignedToTaskException("Este serviço não está vinculado a esta tarefa");
        }

        taskServiceRepository.deleteByTaskIdAndServiceId(taskId, serviceId);
        recalculateTaskCosts(taskId);
    }

    /**
     * Lista todos os serviços vinculados a uma tarefa.
     * 
     * @param taskId ID da tarefa
     * @return List<TaskServiceDto> lista de serviços da tarefa
     */
    @Transactional(readOnly = true)
    public List<TaskServiceDto> getTaskServices(Long taskId) {
        return taskServiceRepository.findByTaskIdWithServiceDetails(taskId).stream()
                .map(this::convertTaskServiceToDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o progresso de uma tarefa.
     * 
     * Atualiza o percentual de progresso da tarefa e opcionalmente
     * as horas reais trabalhadas. Também pode adicionar notas sobre
     * o progresso. Após a atualização, recalcula o progresso do projeto.
     * 
     * @param taskId ID da tarefa
     * @param progressUpdateDto dados de atualização do progresso
     * @return TaskViewDto tarefa atualizada
     * @throws TaskNotFoundException se a tarefa não for encontrada
     */
    public TaskViewDto updateTaskProgress(Long taskId, TaskProgressUpdateDto progressUpdateDto) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa não encontrada"));

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

    /**
     * Recalcula os custos de uma tarefa baseado nos serviços vinculados.
     * 
     * Calcula separadamente os custos de mão de obra, materiais e equipamentos
     * somando os custos de todos os serviços vinculados à tarefa multiplicados
     * pelas suas respectivas quantidades. Após o recálculo, atualiza também
     * os custos realizados do projeto.
     * 
     * @param taskId ID da tarefa
     * @throws TaskNotFoundException se a tarefa não for encontrada
     */
    public void recalculateTaskCosts(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa não encontrada"));

        BigDecimal laborCost = taskServiceRepository.calculateLaborCostByTaskId(taskId);
        BigDecimal materialCost = taskServiceRepository.calculateMaterialCostByTaskId(taskId);
        BigDecimal equipmentCost = taskServiceRepository.calculateEquipmentCostByTaskId(taskId);

        task.updateCosts(laborCost, materialCost, equipmentCost);
        taskRepository.save(task);

        // Recalcular custos realizados do projeto
        projectService.recalculateProjectRealizedCost(task.getProject().getId());
    }

    /**
     * Gera relatório de custos de uma tarefa.
     * 
     * Cria um relatório detalhado dos custos de uma tarefa incluindo
     * todos os serviços vinculados e seus respectivos custos.
     * 
     * @param taskId ID da tarefa
     * @return TaskCostReportDto relatório de custos da tarefa
     * @throws TaskNotFoundException se a tarefa não for encontrada
     */
    @Transactional(readOnly = true)
    public TaskCostReportDto getTaskCostReport(Long taskId) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa não encontrada"));

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

    /**
     * Converte entidade Service para ServiceDto.
     * 
     * @param service entidade do serviço
     * @return ServiceDto dados do serviço para API
     */
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

    /**
     * Converte entidade TaskService para TaskServiceDto.
     * 
     * @param taskService entidade do serviço da tarefa
     * @return TaskServiceDto dados do serviço da tarefa para API
     */
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