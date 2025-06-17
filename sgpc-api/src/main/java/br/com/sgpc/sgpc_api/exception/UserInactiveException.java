package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um usuário inativo tenta fazer login.
 */
public class UserInactiveException extends RuntimeException {
    public UserInactiveException(String message) {
        super(message);
    }
} 