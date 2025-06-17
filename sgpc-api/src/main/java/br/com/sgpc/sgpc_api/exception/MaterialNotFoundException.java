package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um material não é encontrado.
 */
public class MaterialNotFoundException extends RuntimeException {
    public MaterialNotFoundException(String message) {
        super(message);
    }
} 