package br.com.sgpc.sgpc_api.enums;

public enum ProjectStatus {
    PLANEJAMENTO("Planejamento"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído"),
    SUSPENSO("Suspenso"),
    CANCELADO("Cancelado");

    private final String description;

    ProjectStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        
        for (ProjectStatus projectStatus : ProjectStatus.values()) {
            if (projectStatus.name().equalsIgnoreCase(status)) {
                return projectStatus;
            }
        }
        
        throw new IllegalArgumentException("Status de projeto inválido: " + status);
    }
} 