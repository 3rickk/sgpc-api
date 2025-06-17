package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um usuário não é encontrado.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
} 