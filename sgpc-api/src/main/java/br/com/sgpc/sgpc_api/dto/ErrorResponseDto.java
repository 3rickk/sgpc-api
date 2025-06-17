package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDateTime;

/**
 * DTO para padronizar respostas de erro da API.
 */
public class ErrorResponseDto {
    private int status;
    private String erro;
    private String mensagem;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDto(int status, String erro, String mensagem, String path) {
        this();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.path = path;
    }

    // Getters e Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 