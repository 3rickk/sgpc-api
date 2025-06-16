package br.com.sgpc.sgpc_api.enums;

/**
 * Enumeração que representa os possíveis status de uma tarefa.
 * 
 * Define os estados pelos quais uma tarefa pode passar durante
 * seu ciclo de vida no sistema SGPC, desde a criação até a conclusão.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
public enum TaskStatus {
    
    /**
     * Tarefa ainda não iniciada.
     * 
     * Status inicial para tarefas que foram criadas mas ainda
     * não tiveram sua execução iniciada.
     */
    A_FAZER("A Fazer"),
    
    /**
     * Tarefa em execução.
     * 
     * Status para tarefas que estão sendo ativamente executadas
     * pela equipe responsável.
     */
    EM_ANDAMENTO("Em Andamento"),
    
    /**
     * Tarefa finalizada.
     * 
     * Status para tarefas que foram completadas com sucesso
     * e entregues conforme especificado.
     */
    CONCLUIDA("Concluída"),
    
    /**
     * Tarefa impedida de prosseguir.
     * 
     * Status para tarefas que não podem continuar devido a
     * dependências, falta de recursos ou outros impedimentos.
     */
    BLOQUEADA("Bloqueada"),
    
    /**
     * Tarefa cancelada.
     * 
     * Status para tarefas que foram canceladas e não serão
     * mais executadas por decisão do projeto.
     */
    CANCELADA("Cancelada");

    /**
     * Descrição legível do status.
     */
    private final String description;

    /**
     * Construtor do enum TaskStatus.
     * 
     * @param description descrição legível do status
     */
    TaskStatus(String description) {
        this.description = description;
    }

    /**
     * Obtém a descrição legível do status.
     * 
     * @return String descrição do status para exibição
     */
    public String getDescription() {
        return description;
    }

    /**
     * Converte uma string para o enum TaskStatus correspondente.
     * 
     * Realiza busca case-insensitive pelo nome do enum.
     * 
     * @param status string representando o status
     * @return TaskStatus enum correspondente ou null se status for null
     * @throws IllegalArgumentException se o status não for válido
     */
    public static TaskStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.name().equalsIgnoreCase(status)) {
                return taskStatus;
            }
        }
        
        throw new IllegalArgumentException("Status de tarefa inválido: " + status);
    }
} 