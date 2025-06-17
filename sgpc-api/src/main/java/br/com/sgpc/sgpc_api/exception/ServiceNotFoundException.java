package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um serviço não é encontrado.
 */
public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String message) {
        super(message);
    }
} 