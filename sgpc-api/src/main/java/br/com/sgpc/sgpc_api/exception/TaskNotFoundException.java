package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando uma tarefa não é encontrada.
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
} 