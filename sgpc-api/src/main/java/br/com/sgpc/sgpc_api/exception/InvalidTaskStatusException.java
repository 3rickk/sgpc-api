package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um status de tarefa fornecido é inválido.
 */
public class InvalidTaskStatusException extends RuntimeException {
    public InvalidTaskStatusException(String message) {
        super(message);
    }
} 