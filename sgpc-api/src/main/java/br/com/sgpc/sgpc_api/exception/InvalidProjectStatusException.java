package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um status de projeto fornecido é inválido.
 */
public class InvalidProjectStatusException extends RuntimeException {
    public InvalidProjectStatusException(String message) {
        super(message);
    }
} 