package br.com.sgpc.sgpc_api.enums;

public enum RequestStatus {
    PENDENTE("Pendente"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RequestStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        
        for (RequestStatus requestStatus : RequestStatus.values()) {
            if (requestStatus.name().equalsIgnoreCase(status)) {
                return requestStatus;
            }
        }
        
        throw new IllegalArgumentException("Status de requisição inválido: " + status);
    }
} 