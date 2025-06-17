package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de remover um serviço que não está atribuído à tarefa.
 */
public class ServiceNotAssignedToTaskException extends RuntimeException {
    public ServiceNotAssignedToTaskException(String message) {
        super(message);
    }
} 