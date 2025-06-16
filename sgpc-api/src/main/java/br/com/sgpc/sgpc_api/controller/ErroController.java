package br.com.sgpc.sgpc_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller responsável pelo tratamento de erros HTTP globais.
 * 
 * Este controller implementa o Spring Boot ErrorController para
 * capturar erros não tratados e retornar respostas padronizadas
 * em formato JSON para a API.
 * 
 * Funcionalidades:
 * - Captura erros 404, 500 e outros códigos HTTP
 * - Retorna respostas padronizadas em JSON
 * - Inclui informações de diagnóstico como path e status
 * - Integra com o sistema de logs do Spring Boot
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Controller
@Tag(name = "Error Handler", description = "Manipulador global de erros HTTP")
public class ErroController implements ErrorController {

    /**
     * Endpoint para tratamento de erros HTTP.
     * 
     * Captura todos os erros não tratados na aplicação e retorna
     * uma resposta padronizada com informações sobre o erro.
     * 
     * @param request requisição HTTP que gerou o erro
     * @return ResponseEntity com detalhes do erro
     */
    @RequestMapping("/error")
    @ResponseBody
    @Operation(summary = "Tratamento de erros HTTP", 
               description = "Endpoint automático para tratamento de erros não capturados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", 
                     description = "Rota não encontrada",
                     content = @Content(
                         mediaType = "application/json",
                         schema = @Schema(implementation = Map.class),
                         examples = @ExampleObject(
                             value = "{\"status\": 404, \"mensagem\": \"Rota não encontrada\", \"path\": \"/api/inexistente\"}"
                         )
                     )),
        @ApiResponse(responseCode = "500", 
                     description = "Erro interno do servidor",
                     content = @Content(
                         mediaType = "application/json",
                         schema = @Schema(implementation = Map.class),
                         examples = @ExampleObject(
                             value = "{\"status\": 500, \"mensagem\": \"Erro interno do servidor\", \"path\": \"/api/endpoint\"}"
                         )
                     )),
        @ApiResponse(responseCode = "4XX", 
                     description = "Outros erros de cliente",
                     content = @Content(
                         mediaType = "application/json",
                         schema = @Schema(implementation = Map.class),
                         examples = @ExampleObject(
                             value = "{\"status\": 400, \"mensagem\": \"Ocorreu um erro inesperado\", \"path\": \"/api/endpoint\"}"
                         )
                     ))
    })
    @Hidden // Oculta este endpoint da documentação Swagger principal
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String mensagem;

        // Mapeia códigos de status para mensagens amigáveis
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            mensagem = "Rota não encontrada";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            mensagem = "Erro interno do servidor";
        } else {
            mensagem = "Ocorreu um erro inesperado";
        }

        // Monta resposta padronizada
        errorResponse.put("status", statusCode);
        errorResponse.put("mensagem", mensagem);
        errorResponse.put("path", request.getAttribute("javax.servlet.error.request_uri"));

        return ResponseEntity
            .status(statusCode)
            .body(errorResponse);
    }
}
