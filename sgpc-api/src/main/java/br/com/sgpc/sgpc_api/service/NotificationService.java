package br.com.sgpc.sgpc_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.sgpc.sgpc_api.entity.MaterialRequest;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.entity.User;

/**
 * Serviço responsável pelo sistema de notificações por email.
 * 
 * Este serviço gerencia o envio de notificações automáticas para usuários
 * do sistema SGPC, incluindo alertas de tarefas, aprovações de materiais,
 * atrasos em projetos e outros eventos importantes do sistema.
 * 
 * Funcionalidades principais:
 * - Notificações de requisições de material
 * - Alertas de tarefas atribuídas e em atraso
 * - Notificações de aprovação/rejeição
 * - Alertas de orçamento e prazos
 * - Execução assíncrona para performance
 * - Configuração habilitável/desabilitável
 * 
 * Características especiais:
 * - Processamento assíncrono com @Async
 * - Templates de email padronizados
 * - Configuração flexível via properties
 * - Log detalhado de envios e erros
 * - Integração com Spring Mail
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${app.mail.from:noreply@sgpc.com}")
    private String fromEmail;

    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;

    /**
     * Notifica aprovadores sobre nova requisição de material.
     * 
     * Envia email assíncrono para todos os usuários com permissão
     * de aprovação quando uma nova requisição é criada.
     * 
     * @param request requisição de material criada
     * @param approvers lista de usuários aprovadores
     */
    @Async
    public void notifyNewMaterialRequest(MaterialRequest request, List<User> approvers) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Nova Requisição de Material - " + request.getProject().getName();
        String message = String.format(
            "Uma nova requisição de material foi criada:\n\n" +
            "Projeto: %s\n" +
            "Solicitante: %s\n" +
            "Data de Necessidade: %s\n" +
            "Status: %s\n\n" +
            "Acesse o sistema para mais detalhes.",
            request.getProject().getName(),
            request.getRequester().getFullName(),
            request.getNeededDate(),
            request.getStatus()
        );

        for (User approver : approvers) {
            sendEmail(approver.getEmail(), subject, message);
        }
    }

    /**
     * Notifica usuário sobre nova tarefa atribuída.
     * 
     * Envia email automático quando uma tarefa é atribuída
     * a um usuário específico.
     * 
     * @param task tarefa atribuída
     * @param assignee usuário que recebeu a atribuição
     */
    @Async
    public void notifyTaskAssigned(Task task, User assignee) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Nova Tarefa Atribuída - " + task.getTitle();
        String message = String.format(
            "Uma nova tarefa foi atribuída para você:\n\n" +
            "Projeto: %s\n" +
            "Tarefa: %s\n" +
            "Prioridade: %s\n\n" +
            "Acesse o sistema para mais detalhes.",
            task.getProject().getName(),
            task.getTitle(),
            task.getPriorityDescription()
        );

        sendEmail(assignee.getEmail(), subject, message);
    }

    /**
     * Notifica solicitante sobre mudança de status da requisição.
     * 
     * Informa quando uma requisição de material é aprovada
     * ou rejeitada pelos responsáveis.
     * 
     * @param request requisição com status alterado
     */
    @Async
    public void notifyMaterialRequestStatusChanged(MaterialRequest request) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Requisição " + request.getStatus().toString() + " - " + request.getProject().getName();
        String message = String.format(
            "Sua requisição de material foi %s:\n\n" +
            "Projeto: %s\n" +
            "Status: %s\n" +
            "Data de Necessidade: %s\n\n" +
            "Acesse o sistema para mais detalhes.",
            request.getStatus().toString().toLowerCase(),
            request.getProject().getName(),
            request.getStatus(),
            request.getNeededDate()
        );

        sendEmail(request.getRequester().getEmail(), subject, message);
    }

    /**
     * Alerta sobre tarefa em atraso.
     * 
     * Notifica o usuário responsável quando uma tarefa
     * ultrapassa a data prevista de conclusão.
     * 
     * @param task tarefa em atraso
     */
    @Async
    public void notifyTaskOverdue(Task task) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Tarefa em Atraso - " + task.getTitle();
        String message = String.format(
            "A seguinte tarefa está em atraso:\n\n" +
            "Projeto: %s\n" +
            "Tarefa: %s\n" +
            "Data de Fim Prevista: %s\n" +
            "Status: %s\n\n" +
            "Por favor, atualize o status da tarefa no sistema.",
            task.getProject().getName(),
            task.getTitle(),
            task.getEndDatePlanned(),
            task.getStatus()
        );

        if (task.getAssignedUser() != null) {
            sendEmail(task.getAssignedUser().getEmail(), subject, message);
        }
    }

    /**
     * Alerta sobre estouro de orçamento do projeto.
     * 
     * Notifica o gerente quando o projeto se aproxima
     * ou ultrapassa o orçamento previsto.
     * 
     * @param project projeto com orçamento comprometido
     * @param projectManager gerente responsável pelo projeto
     */
    @Async
    public void notifyBudgetOverrun(Project project, User projectManager) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Alerta de Orçamento - " + project.getName();
        String message = String.format(
            "O projeto está próximo ou excedeu o orçamento:\n\n" +
            "Projeto: %s\n" +
            "Orçamento Total: R$ %.2f\n" +
            "Status: %s\n\n" +
            "Verifique os gastos do projeto no sistema.",
            project.getName(),
            project.getTotalBudget(),
            project.getStatus()
        );

        sendEmail(projectManager.getEmail(), subject, message);
    }

    /**
     * Envia email com token de recuperação de senha.
     * 
     * Envia email com instruções e token para redefinição de senha.
     * 
     * @param email endereço de email do usuário
     * @param token token de recuperação gerado
     */
    @Async
    public void sendPasswordResetEmail(String email, String token) {
        if (!notificationEnabled) {
            logger.info("Notificações desabilitadas");
            return;
        }

        String subject = "Recuperação de Senha - SGPC";
        String message = String.format(
            "Você solicitou a recuperação de sua senha.\n\n" +
            "Use o token abaixo para redefinir sua senha:\n\n" +
            "Token: %s\n\n" +
            "Este token é válido por 24 horas.\n\n" +
            "Se você não solicitou esta recuperação, ignore este email.\n\n" +
            "Equipe SGPC",
            token
        );

        sendEmail(email, subject, message);
    }

    /**
     * Método privado para envio de email.
     * 
     * Configura e envia mensagem usando JavaMailSender.
     * Inclui tratamento de erros e logging de operações.
     * 
     * @param to endereço de email destinatário
     * @param subject assunto da mensagem
     * @param text corpo da mensagem
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            emailSender.send(message);
            logger.info("Email enviado para: {}", to);
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", to, e.getMessage());
        }
    }
} 