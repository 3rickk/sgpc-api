package br.com.sgpc.sgpc_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sgpc.sgpc_api.entity.AuditLog;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.AuditLogRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Registra uma atividade de auditoria
     */
    public void logActivity(String entityType, Long entityId, String operation, 
                           Object oldValue, Object newValue, String userIp) {
        try {
            User currentUser = getCurrentUser();
            
            String oldValuesJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newValuesJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;

            AuditLog auditLog = new AuditLog(
                entityType,
                entityId,
                operation,
                currentUser,
                userIp,
                oldValuesJson,
                newValuesJson
            );

            auditLogRepository.save(auditLog);
            logger.info("Log de auditoria registrado: {} {} para entidade {} ID {}", 
                       operation, entityType, entityType, entityId);

        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar dados para log de auditoria: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao registrar log de auditoria: {}", e.getMessage());
        }
    }

    /**
     * Método simplificado para registrar atividades sem valores antigos/novos
     */
    public void logActivity(String entityType, Long entityId, String operation, String userIp) {
        logActivity(entityType, entityId, operation, null, null, userIp);
    }

    /**
     * Buscar logs de auditoria por entidade
     */
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Buscar logs de auditoria por usuário
     */
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Buscar logs de auditoria por período
     */
    public List<AuditLog> getAuditLogsByPeriod(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    /**
     * Buscar logs de auditoria com filtros e paginação
     */
    public Page<AuditLog> getAuditLogsByFilters(String entityType, Long userId, String operation, 
                                               LocalDateTime startDate, LocalDateTime endDate, 
                                               Pageable pageable) {
        return auditLogRepository.findAuditLogsByFilters(entityType, userId, operation, 
                                                        startDate, endDate, pageable);
    }

    /**
     * Obter usuário autenticado atual
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Não foi possível obter usuário autenticado: {}", e.getMessage());
        }
        return null;
    }
} 