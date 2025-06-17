package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um serviço já está atribuído a uma tarefa.
 */
public class ServiceAlreadyAssignedException extends RuntimeException {
    public ServiceAlreadyAssignedException(String message) {
        super(message);
    }
} 