package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando há erro no carregamento dos dados do dashboard.
 */
public class DashboardDataException extends RuntimeException {
    public DashboardDataException(String message) {
        super(message);
    }
    
    public DashboardDataException(String message, Throwable cause) {
        super(message, cause);
    }
} 