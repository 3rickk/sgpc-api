package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando datas fornecidas são inválidas.
 */
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
} 