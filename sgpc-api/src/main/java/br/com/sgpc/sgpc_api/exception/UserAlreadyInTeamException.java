package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de adicionar um usuário que já está na equipe do projeto.
 */
public class UserAlreadyInTeamException extends RuntimeException {
    public UserAlreadyInTeamException(String message) {
        super(message);
    }
} 