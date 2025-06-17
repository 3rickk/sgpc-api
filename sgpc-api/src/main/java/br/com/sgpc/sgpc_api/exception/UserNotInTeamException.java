package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de remover um usuário que não está na equipe do projeto.
 */
public class UserNotInTeamException extends RuntimeException {
    public UserNotInTeamException(String message) {
        super(message);
    }
} 