package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando tentativa de criar um material com nome já existente.
 */
public class MaterialAlreadyExistsException extends RuntimeException {
    public MaterialAlreadyExistsException(String message) {
        super(message);
    }
} 