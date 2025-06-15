package br.com.sgpc.sgpc_api.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.sgpc.sgpc_api.entity.MaterialRequest;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditLogService auditLogService;

    // Intercepta criação de projetos
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.create*(..))", returning = "result")
    public void auditProjectCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof Project) {
            Project project = (Project) result;
            auditLogService.logActivity("Project", project.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Projeto criado - ID: {}, Nome: {}", project.getId(), project.getName());
        }
    }

    // Intercepta atualização de projetos
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.update*(..))", returning = "result")
    public void auditProjectUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Project) {
            Project project = (Project) result;
            auditLogService.logActivity("Project", project.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Projeto atualizado - ID: {}, Nome: {}", project.getId(), project.getName());
        }
    }

    // Intercepta exclusão de projetos
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.delete*(..))")
    public void auditProjectDeletion(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            Long projectId = (Long) args[0];
            auditLogService.logActivity("Project", projectId, "DELETE", getClientIp());
            logger.info("Auditoria: Projeto deletado - ID: {}", projectId);
        }
    }

    // Intercepta criação de tarefas
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.TaskService.create*(..))", returning = "result")
    public void auditTaskCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            auditLogService.logActivity("Task", task.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Tarefa criada - ID: {}, Título: {}", task.getId(), task.getTitle());
        }
    }

    // Intercepta atualização de tarefas
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.TaskService.update*(..))", returning = "result")
    public void auditTaskUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            auditLogService.logActivity("Task", task.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Tarefa atualizada - ID: {}, Título: {}", task.getId(), task.getTitle());
        }
    }

    // Intercepta criação de requisições de material
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.MaterialRequestService.create*(..))", returning = "result")
    public void auditMaterialRequestCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof MaterialRequest) {
            MaterialRequest request = (MaterialRequest) result;
            auditLogService.logActivity("MaterialRequest", request.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Requisição de material criada - ID: {}", request.getId());
        }
    }

    // Intercepta aprovação/rejeição de requisições
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.MaterialRequestService.approve*(..))" +
                             " || execution(* br.com.sgpc.sgpc_api.service.MaterialRequestService.reject*(..))", 
                   returning = "result")
    public void auditMaterialRequestApproval(JoinPoint joinPoint, Object result) {
        if (result instanceof MaterialRequest) {
            MaterialRequest request = (MaterialRequest) result;
            String operation = joinPoint.getSignature().getName().toUpperCase();
            auditLogService.logActivity("MaterialRequest", request.getId(), operation, getClientIp());
            logger.info("Auditoria: Requisição de material {} - ID: {}", operation, request.getId());
        }
    }

    // Intercepta criação de usuários
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.UserService.create*(..))", returning = "result")
    public void auditUserCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof User) {
            User user = (User) result;
            auditLogService.logActivity("User", user.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Usuário criado - ID: {}, Email: {}", user.getId(), user.getEmail());
        }
    }

    // Intercepta atualização de usuários
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.UserService.update*(..))", returning = "result")
    public void auditUserUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof User) {
            User user = (User) result;
            auditLogService.logActivity("User", user.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Usuário atualizado - ID: {}, Email: {}", user.getId(), user.getEmail());
        }
    }

    /**
     * Obtém o IP do cliente da requisição HTTP atual
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        } catch (Exception e) {
            logger.warn("Não foi possível obter IP do cliente: {}", e.getMessage());
            return "unknown";
        }
    }
} 