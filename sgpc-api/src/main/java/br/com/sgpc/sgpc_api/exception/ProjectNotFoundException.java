package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um projeto não é encontrado.
 */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
} 