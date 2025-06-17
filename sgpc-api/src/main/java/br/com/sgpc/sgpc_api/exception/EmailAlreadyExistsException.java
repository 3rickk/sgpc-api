package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um email já está em uso no sistema.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
} 