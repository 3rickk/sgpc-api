package br.com.sgpc.sgpc_api.controller;// caminho do pacote

import org.springframework.web.bind.annotation.GetMapping; // para rotas GET
import org.springframework.web.bind.annotation.RestController; // para dizer que é um controller

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de teste e boas-vindas da API SGPC.
 * 
 * Este foi o primeiro controller criado para testar o funcionamento
 * básico do Spring Boot durante o desenvolvimento inicial.
 * Mantido para fins de histórico e testes básicos.
 * 
 * Funcionalidades:
 * - Endpoint de boas-vindas
 * - Teste básico de funcionamento da API
 * - Verificação de configuração do Spring Boot
 * - Endpoint público sem autenticação
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024 (primeiro controller do projeto)
 */
@RestController // indica que é um controller que retorna JSON (API REST)
@Tag(name = "Hello World", description = "Endpoint de teste e boas-vindas")
public class HelloController {

    /**
     * Endpoint de boas-vindas da API SGPC.
     * 
     * Primeiro endpoint criado no projeto, mantido para fins de
     * histórico e como endpoint de teste básico.
     * 
     * @return String mensagem de boas-vindas com emoji
     */
    @GetMapping("/hello") // quando acessar http://localhost:8080/hello
    @Operation(summary = "Mensagem de boas-vindas", 
               description = "Endpoint de teste que retorna uma mensagem de boas-vindas personalizada")
    @ApiResponse(responseCode = "200", 
                 description = "Mensagem de boas-vindas retornada com sucesso",
                 content = @Content(
                     mediaType = "text/plain",
                     examples = @ExampleObject(value = "Olá, Erick! O backend está funcionando! 🚀")
                 ))
    @Hidden // Oculta da documentação principal por ser endpoint de teste
    public String hello() {
        return "Olá, Erick! O backend está funcionando! 🚀";
    }
}
