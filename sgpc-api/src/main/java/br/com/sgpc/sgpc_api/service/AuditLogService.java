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

/**
 * Service responsável pelo gerenciamento de logs de auditoria do sistema SGPC.
 * 
 * Esta classe centraliza todas as operações relacionadas ao registro, consulta
 * e análise de atividades realizadas no sistema para fins de auditoria,
 * conformidade e rastreabilidade.
 * 
 * Funcionalidades principais:
 * - Registro automático de atividades CRUD
 * - Serialização de valores antigos/novos para comparação
 * - Captura de informações de contexto (usuário, IP, timestamp)
 * - Consultas avançadas por filtros múltiplos
 * - Integração com Spring Security para identificação de usuários
 * - Suporte a paginação para grandes volumes de dados
 * 
 * Benefícios:
 * - Rastreabilidade completa de operações
 * - Conformidade com regulamentações
 * - Detecção de atividades suspeitas
 * - Análise de comportamento de usuários
 * - Recuperação de informações históricas
 * - Suporte a investigações e troubleshooting
 * 
 * Integração com AOP:
 * Esta classe é chamada automaticamente pelo AuditAspect via
 * interceptação de métodos, garantindo auditoria transparente.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
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
     * Registra uma atividade de auditoria completa com valores antigos e novos.
     * 
     * Este método permite registrar mudanças detalhadas capturando o estado
     * anterior e posterior de uma entidade, permitindo análise completa
     * de modificações.
     * 
     * @param entityType tipo da entidade (ex: "Project", "Task", "User")
     * @param entityId ID da entidade modificada
     * @param operation operação realizada (CREATE, UPDATE, DELETE, etc.)
     * @param oldValue valor anterior da entidade (pode ser null)
     * @param newValue valor atual da entidade (pode ser null)
     * @param userIp endereço IP do usuário que executou a operação
     */
    public void logActivity(String entityType, Long entityId, String operation, 
                           Object oldValue, Object newValue, String userIp) {
        try {
            User currentUser = getCurrentUser();
            
            // Serializa valores para JSON para armazenamento estruturado
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
     * Método simplificado para registrar atividades sem valores antigos/novos.
     * 
     * Versão simplificada do método principal, útil para operações onde
     * não é necessário rastrear mudanças específicas (ex: CREATE, DELETE).
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @param operation operação realizada
     * @param userIp endereço IP do usuário
     */
    public void logActivity(String entityType, Long entityId, String operation, String userIp) {
        logActivity(entityType, entityId, operation, null, null, userIp);
    }

    /**
     * Busca logs de auditoria por entidade específica.
     * 
     * Permite rastrear o histórico completo de modificações de uma
     * entidade específica, ordenado por timestamp descendente.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @return List<AuditLog> logs da entidade ordenados por data
     */
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Busca logs de auditoria por usuário.
     * 
     * Permite rastrear todas as atividades realizadas por um usuário
     * específico, útil para análise de comportamento e investigações.
     * 
     * @param userId ID do usuário
     * @return List<AuditLog> logs do usuário ordenados por data
     */
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Busca logs de auditoria por período específico.
     * 
     * Permite análise de atividades em janelas temporais específicas,
     * útil para relatórios periódicos e análise de tendências.
     * 
     * @param start data/hora de início do período
     * @param end data/hora de fim do período
     * @return List<AuditLog> logs no período ordenados por data
     */
    public List<AuditLog> getAuditLogsByPeriod(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    /**
     * Busca logs de auditoria com filtros avançados e paginação.
     * 
     * Método mais completo que permite combinação de múltiplos filtros
     * com suporte a paginação para grandes volumes de dados.
     * 
     * Filtros disponíveis:
     * - Tipo de entidade (opcional)
     * - Usuário (opcional)
     * - Operação (opcional)
     * - Período (opcional)
     * 
     * @param entityType tipo da entidade (null para todos)
     * @param userId ID do usuário (null para todos)
     * @param operation operação (null para todas)
     * @param startDate data de início (null para sem limite)
     * @param endDate data de fim (null para sem limite)
     * @param pageable configuração de paginação
     * @return Page<AuditLog> página de logs com filtros aplicados
     */
    public Page<AuditLog> getAuditLogsByFilters(String entityType, Long userId, String operation, 
                                               LocalDateTime startDate, LocalDateTime endDate, 
                                               Pageable pageable) {
        return auditLogRepository.findAuditLogsByFilters(entityType, userId, operation, 
                                                        startDate, endDate, pageable);
    }

    /**
     * Obtém o usuário autenticado atual do contexto do Spring Security.
     * 
     * Extrai informações do usuário logado a partir do SecurityContext
     * para associar automaticamente as atividades ao usuário correto.
     * 
     * Tratamento de erros:
     * - Retorna null se não há usuário autenticado
     * - Retorna null se o usuário não é encontrado no banco
     * - Log de warning em caso de erro
     * 
     * @return User usuário autenticado ou null se não encontrado
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