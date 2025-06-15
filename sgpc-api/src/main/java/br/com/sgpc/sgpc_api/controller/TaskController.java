package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.TaskCreateDto;
import br.com.sgpc.sgpc_api.dto.TaskUpdateStatusDto;
import br.com.sgpc.sgpc_api.dto.TaskViewDto;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.service.TaskService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskViewDto> createTask(
            @PathVariable Long projectId,
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

    @GetMapping
    public ResponseEntity<List<TaskViewDto>> getTasksByProject(@PathVariable Long projectId) {
        List<TaskViewDto> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskViewDto> getTaskById(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskViewDto> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCreateDto taskUpdateDto) {
        try {
            TaskViewDto updatedTask = taskService.updateTask(taskId, taskUpdateDto);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar tarefa: " + e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar tarefa: " + e.getMessage());
        }
    }

    // Endpoint específico para atualização de status (drag-and-drop do Kanban)
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskViewDto> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateStatusDto statusUpdateDto) {
        try {
            TaskViewDto updatedTask = taskService.updateTaskStatus(taskId, statusUpdateDto);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar status da tarefa: " + e.getMessage());
        }
    }

    // Endpoints para filtros específicos do Kanban
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskViewDto>> getTasksByStatus(
            @PathVariable Long projectId, 
            @PathVariable String status) {
        try {
            TaskStatus taskStatus = TaskStatus.fromString(status);
            List<TaskViewDto> tasks = taskService.getTasksByProjectAndStatus(projectId, taskStatus);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/kanban")
    public ResponseEntity<KanbanBoardDto> getKanbanBoard(@PathVariable Long projectId) {
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

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskViewDto>> getOverdueTasks(@PathVariable Long projectId) {
        List<TaskViewDto> overdueTasks = taskService.getOverdueTasksByProject(projectId);
        return ResponseEntity.ok(overdueTasks);
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<TaskViewDto>> getTasksByAssignedUser(
            @PathVariable Long projectId, 
            @PathVariable Long userId) {
        List<TaskViewDto> tasks = taskService.getTasksByAssignedUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics(@PathVariable Long projectId) {
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

    // DTOs auxiliares para respostas específicas
    public static class KanbanBoardDto {
        private List<TaskViewDto> aFazer;
        private List<TaskViewDto> emAndamento;
        private List<TaskViewDto> concluidas;
        private List<TaskViewDto> bloqueadas;
        private List<TaskViewDto> canceladas;
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