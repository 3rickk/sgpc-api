package br.com.sgpc.sgpc_api.controller;// caminho do pacote

import org.springframework.web.bind.annotation.GetMapping; // para rotas GET
import org.springframework.web.bind.annotation.RestController; // para dizer que é um controller

@RestController // indica que é um controller que retorna JSON (API REST)
public class HelloController {

    @GetMapping("/hello") // quando acessar http://localhost:8080/hello
    public String hello() {
        return "Olá, Erick! O backend está funcionando! 🚀";
    }
}
