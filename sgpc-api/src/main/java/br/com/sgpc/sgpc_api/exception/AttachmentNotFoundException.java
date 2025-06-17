package br.com.sgpc.sgpc_api.exception;

/**
 * Exceção lançada quando um anexo não é encontrado.
 */
public class AttachmentNotFoundException extends RuntimeException {
    public AttachmentNotFoundException(String message) {
        super(message);
    }
} 