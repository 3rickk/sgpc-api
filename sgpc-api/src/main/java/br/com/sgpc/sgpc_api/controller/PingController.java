package br.com.sgpc.sgpc_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller responsável por verificação de saúde e conectividade da API.
 * 
 * Este controller fornece endpoints simples para verificar se a aplicação
 * está em funcionamento. Útil para:
 * - Monitoramento de aplicações
 * - Health checks em containers Docker
 * - Verificação de conectividade de rede
 * - Testes de integração básicos
 * 
 * Não requer autenticação para permitir verificações externas.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@Tag(name = "Health Check", description = "Verificação de saúde e conectividade da API")
public class PingController {

    /**
     * Endpoint de verificação de saúde da API.
     * 
     * Retorna uma resposta simples "pong" para confirmar que a aplicação
     * está em funcionamento. Usado para:
     * - Health checks de infraestrutura
     * - Monitoramento de uptime
     * - Verificação de conectividade
     * - Testes automatizados
     * 
     * @return String resposta simples "pong"
     */
    @GetMapping("/ping")
    @Operation(summary = "Verificação de saúde da API", 
               description = "Endpoint simples para verificar se a API está funcionando")
    @ApiResponse(responseCode = "200", 
                 description = "API está funcionando normalmente",
                 content = @Content(
                     mediaType = "text/plain",
                     examples = @ExampleObject(value = "pong")
                 ))
    public String ping() {
        return "pong";
    }
}
