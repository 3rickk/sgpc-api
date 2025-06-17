package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de criar um projeto com nome já existente.
 */
public class ProjectAlreadyExistsException extends RuntimeException {
    public ProjectAlreadyExistsException(String message) {
        super(message);
    }
} 