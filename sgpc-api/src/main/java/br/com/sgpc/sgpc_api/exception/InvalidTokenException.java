package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um token de redefinição de senha é inválido.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
} 