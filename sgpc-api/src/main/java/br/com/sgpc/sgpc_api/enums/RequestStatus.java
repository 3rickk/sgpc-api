package br.com.sgpc.sgpc_api.enums;

/**
 * Enumeração que define os possíveis status de uma solicitação.
 * 
 * Esta enumeração é utilizada para controlar o fluxo de aprovação
 * de solicitações no sistema, especialmente solicitações de materiais,
 * desde a criação até a decisão final de aprovação ou rejeição.
 * 
 * Estados possíveis:
 * - PENDENTE: Solicitação criada aguardando análise
 * - APROVADA: Solicitação aprovada e pode ser executada
 * - REJEITADA: Solicitação rejeitada com justificativa
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
public enum RequestStatus {
    
    /**
     * Solicitação pendente de aprovação.
     * 
     * Estado inicial onde a solicitação foi criada e está
     * aguardando análise e decisão de um aprovador.
     */
    PENDENTE("Pendente"),
    
    /**
     * Solicitação aprovada.
     * 
     * Estado onde a solicitação foi analisada e aprovada,
     * podendo proceder com a execução do solicitado.
     */
    APROVADA("Aprovada"),
    
    /**
     * Solicitação rejeitada.
     * 
     * Estado final onde a solicitação foi analisada e rejeitada,
     * geralmente acompanhada de justificativa do motivo.
     */
    REJEITADA("Rejeitada");

    /**
     * Descrição legível do status da solicitação.
     */
    private final String description;

    /**
     * Construtor do enum.
     * 
     * @param description descrição legível do status
     */
    RequestStatus(String description) {
        this.description = description;
    }

    /**
     * Obtém a descrição legível do status.
     * 
     * @return String descrição do status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Converte string para o enum correspondente.
     * 
     * Aceita tanto o nome do enum quanto a descrição
     * para facilitar a conversão de dados externos.
     * 
     * @param value string a ser convertida
     * @return RequestStatus enum correspondente
     * @throws IllegalArgumentException se o valor não for válido
     */
    public static RequestStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status da solicitação não pode ser nulo");
        }
        
        // Tenta primeiro pelo nome do enum
        try {
            return RequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Se não encontrar pelo nome, tenta pela descrição
            for (RequestStatus status : RequestStatus.values()) {
                if (status.description.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Status de solicitação inválido: " + value);
        }
    }

    /**
     * Verifica se a solicitação está pendente de decisão.
     * 
     * @return true se a solicitação está pendente
     */
    public boolean isPending() {
        return this == PENDENTE;
    }

    /**
     * Verifica se a solicitação foi finalizada (aprovada ou rejeitada).
     * 
     * @return true se a solicitação foi finalizada
     */
    public boolean isFinalized() {
        return this == APROVADA || this == REJEITADA;
    }

    /**
     * Verifica se a solicitação foi aprovada.
     * 
     * @return true se a solicitação foi aprovada
     */
    public boolean isApproved() {
        return this == APROVADA;
    }

    /**
     * Verifica se a solicitação foi rejeitada.
     * 
     * @return true se a solicitação foi rejeitada
     */
    public boolean isRejected() {
        return this == REJEITADA;
    }
} 