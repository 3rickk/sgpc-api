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

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username:sgpc@system.com}")
    private String fromEmail;

    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;

    // Notificação para nova requisição de material
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

    // Notificação para tarefa atribuída
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

    // Notificação para requisição aprovada/rejeitada
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

    // Notificação para atraso em tarefa
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

    // Notificação para estouro de orçamento
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

    // Método privado para envio de email
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