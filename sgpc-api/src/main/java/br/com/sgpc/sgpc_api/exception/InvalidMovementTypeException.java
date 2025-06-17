package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um tipo de movimentação de estoque é inválido.
 */
public class InvalidMovementTypeException extends RuntimeException {
    public InvalidMovementTypeException(String message) {
        super(message);
    }
} 