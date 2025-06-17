package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de criar um serviço com nome já existente.
 */
public class ServiceAlreadyExistsException extends RuntimeException {
    public ServiceAlreadyExistsException(String message) {
        super(message);
    }
} 