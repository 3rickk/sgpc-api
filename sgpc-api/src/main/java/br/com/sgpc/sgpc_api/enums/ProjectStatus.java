package br.com.sgpc.sgpc_api.enums;

/**
 * Enumeração que define os possíveis status de um projeto.
 * 
 * Esta enumeração representa o ciclo de vida de um projeto
 * de construção, desde o planejamento até a conclusão,
 * incluindo estados intermediários como pausas e cancelamentos.
 * 
 * Estados possíveis:
 * - PLANEJAMENTO: Projeto em fase de planejamento inicial
 * - EM_ANDAMENTO: Projeto com execução em progresso
 * - PAUSADO: Projeto temporariamente suspenso
 * - CONCLUIDO: Projeto finalizado com sucesso
 * - CANCELADO: Projeto cancelado antes da conclusão
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
public enum ProjectStatus {
    
    /**
     * Projeto em fase de planejamento.
     * 
     * Estado inicial onde o projeto está sendo definido,
     * recursos estão sendo alocados e cronograma está
     * sendo estabelecido.
     */
    PLANEJAMENTO("Planejamento"),
    
    /**
     * Projeto em execução.
     * 
     * Estado onde as tarefas estão sendo executadas
     * ativamente e o progresso está sendo monitorado.
     */
    EM_ANDAMENTO("Em Andamento"),
    
    /**
     * Projeto temporariamente pausado.
     * 
     * Estado onde a execução foi suspensa temporariamente,
     * podendo ser retomada posteriormente.
     */
    PAUSADO("Pausado"),
    
    /**
     * Projeto concluído com sucesso.
     * 
     * Estado final onde todas as tarefas foram finalizadas
     * e os objetivos do projeto foram alcançados.
     */
    CONCLUIDO("Concluído"),
    
    /**
     * Projeto cancelado.
     * 
     * Estado final onde o projeto foi encerrado antes
     * da conclusão, geralmente por decisão estratégica
     * ou problemas técnicos/financeiros.
     */
    CANCELADO("Cancelado");

    /**
     * Descrição legível do status do projeto.
     */
    private final String description;

    /**
     * Construtor do enum.
     * 
     * @param description descrição legível do status
     */
    ProjectStatus(String description) {
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
     * @return ProjectStatus enum correspondente
     * @throws IllegalArgumentException se o valor não for válido
     */
    public static ProjectStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status do projeto não pode ser nulo");
        }
        
        // Tenta primeiro pelo nome do enum
        try {
            return ProjectStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Se não encontrar pelo nome, tenta pela descrição
            for (ProjectStatus status : ProjectStatus.values()) {
                if (status.description.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Status de projeto inválido: " + value);
        }
    }

    /**
     * Verifica se o projeto está em estado ativo (pode ser executado).
     * 
     * @return true se o projeto está em estado ativo
     */
    public boolean isActive() {
        return this == PLANEJAMENTO || this == EM_ANDAMENTO || this == PAUSADO;
    }

    /**
     * Verifica se o projeto está finalizado (não pode mais ser alterado).
     * 
     * @return true se o projeto está finalizado
     */
    public boolean isFinalized() {
        return this == CONCLUIDO || this == CANCELADO;
    }
} 