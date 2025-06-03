package br.com.sgpc.sgpc_api.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String mensagem;

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            mensagem = "Rota não encontrada";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            mensagem = "Erro interno do servidor";
        } else {
            mensagem = "Ocorreu um erro inesperado";
        }

        errorResponse.put("status", statusCode);
        errorResponse.put("mensagem", mensagem);
        errorResponse.put("path", request.getAttribute("javax.servlet.error.request_uri"));

        return ResponseEntity
            .status(statusCode)
            .body(errorResponse);
    }
}
