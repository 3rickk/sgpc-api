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

/**
 * Aspecto para auditoria automática de operações no sistema SGPC.
 * 
 * Esta classe implementa Aspect-Oriented Programming (AOP) para capturar
 * automaticamente operações CRUD importantes e registrar logs de auditoria.
 * Utiliza interceptação transparente via pointcuts do Spring AOP.
 * 
 * Funcionalidades de auditoria:
 * - Projetos: CREATE, UPDATE, DELETE
 * - Tarefas: CREATE, UPDATE
 * - Solicitações de Materiais: CREATE, APPROVE, REJECT
 * - Usuários: CREATE, UPDATE
 * 
 * Informações capturadas:
 * - Tipo de entidade e ID
 * - Operação realizada (CREATE/UPDATE/DELETE)
 * - IP do cliente (com suporte a proxies)
 * - Timestamp automático
 * - Detalhes específicos por entidade
 * 
 * Benefícios:
 * - Auditoria transparente e automática
 * - Rastreabilidade completa de operações
 * - Conformidade com regulamentações
 * - Detecção de atividades suspeitas
 * - Histórico completo para análise
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Intercepta criação de projetos.
     * 
     * Captura automaticamente quando um projeto é criado via ProjectService
     * e registra um log de auditoria com os detalhes da operação.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto Project retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.create*(..))", returning = "result")
    public void auditProjectCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof Project) {
            Project project = (Project) result;
            auditLogService.logActivity("Project", project.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Projeto criado - ID: {}, Nome: {}", project.getId(), project.getName());
        }
    }

    /**
     * Intercepta atualização de projetos.
     * 
     * Captura automaticamente quando um projeto é atualizado via ProjectService
     * e registra um log de auditoria com os detalhes da modificação.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto Project retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.update*(..))", returning = "result")
    public void auditProjectUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Project) {
            Project project = (Project) result;
            auditLogService.logActivity("Project", project.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Projeto atualizado - ID: {}, Nome: {}", project.getId(), project.getName());
        }
    }

    /**
     * Intercepta exclusão de projetos.
     * 
     * Captura automaticamente quando um projeto é excluído via ProjectService.
     * Como o objeto já foi deletado, captura o ID dos argumentos do método.
     * 
     * @param joinPoint informações do método interceptado
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.ProjectService.delete*(..))")
    public void auditProjectDeletion(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            Long projectId = (Long) args[0];
            auditLogService.logActivity("Project", projectId, "DELETE", getClientIp());
            logger.info("Auditoria: Projeto deletado - ID: {}", projectId);
        }
    }

    /**
     * Intercepta criação de tarefas.
     * 
     * Captura automaticamente quando uma tarefa é criada via TaskService
     * e registra um log de auditoria com os detalhes da operação.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto Task retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.TaskService.create*(..))", returning = "result")
    public void auditTaskCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            auditLogService.logActivity("Task", task.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Tarefa criada - ID: {}, Título: {}", task.getId(), task.getTitle());
        }
    }

    /**
     * Intercepta atualização de tarefas.
     * 
     * Captura automaticamente quando uma tarefa é atualizada via TaskService,
     * incluindo mudanças de status, atribuições e outras modificações.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto Task retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.TaskService.update*(..))", returning = "result")
    public void auditTaskUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            auditLogService.logActivity("Task", task.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Tarefa atualizada - ID: {}, Título: {}", task.getId(), task.getTitle());
        }
    }

    /**
     * Intercepta criação de solicitações de materiais.
     * 
     * Captura automaticamente quando uma solicitação de material é criada
     * via MaterialRequestService para rastreabilidade do processo.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto MaterialRequest retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.MaterialRequestService.create*(..))", returning = "result")
    public void auditMaterialRequestCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof MaterialRequest) {
            MaterialRequest request = (MaterialRequest) result;
            auditLogService.logActivity("MaterialRequest", request.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Requisição de material criada - ID: {}", request.getId());
        }
    }

    /**
     * Intercepta aprovação e rejeição de solicitações de materiais.
     * 
     * Captura automaticamente quando uma solicitação é aprovada ou rejeitada
     * via MaterialRequestService, registrando a decisão tomada.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto MaterialRequest retornado pelo método
     */
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

    /**
     * Intercepta criação de usuários.
     * 
     * Captura automaticamente quando um usuário é criado via UserService
     * para rastreabilidade do gerenciamento de usuários.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto User retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.UserService.create*(..))", returning = "result")
    public void auditUserCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof User) {
            User user = (User) result;
            auditLogService.logActivity("User", user.getId(), "CREATE", getClientIp());
            logger.info("Auditoria: Usuário criado - ID: {}, Email: {}", user.getId(), user.getEmail());
        }
    }

    /**
     * Intercepta atualização de usuários.
     * 
     * Captura automaticamente quando um usuário é atualizado via UserService,
     * incluindo mudanças de perfil, roles e outras modificações.
     * 
     * @param joinPoint informações do método interceptado
     * @param result objeto User retornado pelo método
     */
    @AfterReturning(pointcut = "execution(* br.com.sgpc.sgpc_api.service.UserService.update*(..))", returning = "result")
    public void auditUserUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof User) {
            User user = (User) result;
            auditLogService.logActivity("User", user.getId(), "UPDATE", getClientIp());
            logger.info("Auditoria: Usuário atualizado - ID: {}, Email: {}", user.getId(), user.getEmail());
        }
    }

    /**
     * Obtém o IP do cliente da requisição HTTP atual.
     * 
     * Tenta extrair o IP real do cliente considerando proxies e load balancers
     * através dos cabeçalhos X-Forwarded-For e X-Real-IP.
     * 
     * Ordem de prioridade:
     * 1. X-Forwarded-For (primeiro IP da lista)
     * 2. X-Real-IP
     * 3. RemoteAddr direto
     * 
     * @return String IP do cliente ou "unknown" se não conseguir obter
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            
            // Tenta X-Forwarded-For primeiro (para proxies/load balancers)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            // Tenta X-Real-IP como alternativa
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            // Fallback para IP direto
            return request.getRemoteAddr();
        } catch (Exception e) {
            logger.warn("Não foi possível obter IP do cliente: {}", e.getMessage());
            return "unknown";
        }
    }
} 