package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um token de redefinição de senha está expirado.
 */
public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {
        super(message);
    }
} 