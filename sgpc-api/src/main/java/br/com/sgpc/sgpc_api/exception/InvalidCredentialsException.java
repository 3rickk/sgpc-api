package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando as credenciais de login são inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
} 