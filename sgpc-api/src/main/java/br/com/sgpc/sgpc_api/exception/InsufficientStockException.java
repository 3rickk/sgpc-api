package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando há tentativa de operação com estoque insuficiente.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
} 