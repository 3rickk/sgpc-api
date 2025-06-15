package br.com.sgpc.sgpc_api.enums;

public enum TaskStatus {
    A_FAZER("A Fazer"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    BLOQUEADA("Bloqueada"),
    CANCELADA("Cancelada");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

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