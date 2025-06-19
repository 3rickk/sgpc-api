package br.com.sgpc.sgpc_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.JwtResponseDto;
import br.com.sgpc.sgpc_api.dto.LoginRequestDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetRequestDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.service.AuthService;
import br.com.sgpc.sgpc_api.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller responsável pela autenticação e gerenciamento de usuários.
 * 
 * Este controller fornece endpoints para login, registro de novos usuários,
 * recuperação de senha e redefinição de senha. Não requer autenticação JWT
 * para suas operações.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Autenticação", description = "Endpoints para autenticação, registro e recuperação de senha")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * Autentica um usuário no sistema.
     * 
     * Valida as credenciais do usuário (email e senha) e retorna um token JWT
     * válido para acesso aos endpoints protegidos da aplicação.
     * 
     * @param loginRequest dados de login (email e senha)
     * @return ResponseEntity contendo o token JWT e informações do usuário
     */
    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Autentica um usuário no sistema usando email e senha, retornando um token JWT para acesso aos endpoints protegidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = JwtResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"type\": \"Bearer\", \"userId\": 1, \"username\": \"admin\", \"email\": \"admin@sgpc.com\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Dados de login inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"status\": 400, \"erro\": \"Dados inválidos\", \"mensagem\": \"email: não deve estar vazio\", \"path\": \"/api/auth/login\", \"timestamp\": \"2024-01-01T10:00:00\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciais inválidas (email não encontrado ou senha incorreta)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{ \"error\": \"Credenciais inválidas\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Conta inativa",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"status\": 403, \"erro\": \"Conta inativa\", \"mensagem\": \"Conta desativada. Entre em contato com o administrador\", \"path\": \"/api/auth/login\", \"timestamp\": \"2024-01-01T10:00:00\" }"
                )
            )
        )
    })
    public ResponseEntity<JwtResponseDto> authenticateUser(
        @Parameter(description = "Dados de login do usuário", required = true)
        @Valid @RequestBody LoginRequestDto loginRequest) {
        JwtResponseDto response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Registra um novo usuário no sistema.
     * 
     * Cria uma nova conta de usuário com os dados fornecidos, validando
     * se o email já não está em uso e aplicando as regras de negócio
     * para criação de usuários.
     * 
     * @param signUpRequest dados para registro do novo usuário
     * @return ResponseEntity contendo os dados do usuário criado
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar novo administrador",
        description = "Cria uma nova conta de administrador no sistema. Todo usuário que se registra através deste endpoint será criado com role ADMIN automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Administrador registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class),
                examples = @ExampleObject(
                    value = "{ \"id\": 1, \"fullName\": \"João Silva\", \"email\": \"joao@email.com\", \"role\": \"ADMIN\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Dados de registro inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"status\": 400, \"erro\": \"Dados inválidos\", \"mensagem\": \"email: deve ser um endereço de email válido\", \"path\": \"/api/auth/register\", \"timestamp\": \"2024-01-01T10:00:00\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email já cadastrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"status\": 409, \"erro\": \"Email já cadastrado\", \"mensagem\": \"Este email já está cadastrado no sistema\", \"path\": \"/api/auth/register\", \"timestamp\": \"2024-01-01T10:00:00\" }"
                )
            )
        )
    })
    public ResponseEntity<UserDto> registerUser(
        @Parameter(description = "Dados para registro do novo administrador", required = true)
        @Valid @RequestBody UserRegistrationDto signUpRequest) {
        UserDto user = authService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * Solicita recuperação de senha.
     * 
     * Gera um token de recuperação de senha para o email fornecido e envia por email.
     * Por motivos de segurança, sempre retorna a mesma mensagem, independente
     * do email existir ou não no sistema.
     * 
     * @param request dados da solicitação de recuperação (email)
     * @return ResponseEntity com mensagem de confirmação genérica
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Solicitar recuperação de senha",
        description = "Gera um token de recuperação de senha para o email fornecido. Por motivos de segurança, sempre retorna a mesma mensagem, independente do email existir ou não no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Solicitação processada com sucesso (sempre retorna 200 para evitar enumeração de usuários)",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "Se o email informado estiver cadastrado, um token de recuperação foi enviado para ele."
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Dados inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{ \"status\": 400, \"erro\": \"Dados inválidos\", \"mensagem\": \"email: não deve estar vazio\", \"path\": \"/api/auth/forgot-password\", \"timestamp\": \"2024-01-01T10:00:00\" }"
                )
            )
        )
    })
    public ResponseEntity<String> forgotPassword(
        @Parameter(description = "Email para recuperação de senha", required = true)
        @Valid @RequestBody PasswordResetRequestDto request) {
        passwordResetService.generatePasswordResetToken(request);
        return ResponseEntity.ok("Se o email informado estiver cadastrado, um token de recuperação foi enviado para ele.");
    }
    
    /**
     * Redefine a senha do usuário.
     * 
     * Utiliza o token de recuperação para redefinir a senha do usuário.
     * O token deve ser válido e não expirado.
     * 
     * @param resetDto dados para redefinição da senha (token e nova senha)
     * @return ResponseEntity com mensagem de confirmação
     */
    @PostMapping("/reset-password")
    @Operation(
        summary = "Redefinir senha",
        description = "Redefine a senha do usuário usando um token de recuperação válido. O token deve ter sido gerado pelo endpoint /forgot-password e não pode estar expirado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Senha redefinida com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{ \"message\": \"Senha redefinida com sucesso.\" }")
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Token inválido, expirado ou senha não atende aos critérios",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{ \"error\": \"Token de redefinição inválido ou expirado.\" }"
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> resetPassword(
        @Parameter(description = "Dados para redefinição da senha", required = true)
        @Valid @RequestBody PasswordResetDto resetDto) {
        passwordResetService.resetPassword(resetDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Senha redefinida com sucesso.");
        return ResponseEntity.ok(response);
    }
} 