package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.exception.UserNotFoundException;
import br.com.sgpc.sgpc_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller responsável pelo gerenciamento de usuários.
 * 
 * Este controller fornece endpoints para CRUD de usuários,
 * incluindo listagem, visualização, atualização e
 * ativação/desativação de contas de usuário.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Gerenciamento de Usuários", description = "Endpoints para gestão de usuários do sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Lista todos os usuários do sistema.
     * 
     * @return List<UserDto> lista de todos os usuários
     */
    @Operation(
        summary = "Listar todos os usuários",
        description = "Retorna lista completa de todos os usuários cadastrados no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar usuários\",\"path\":\"/api/users\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Lista apenas os usuários ativos.
     * 
     * @return List<UserDto> lista de usuários ativos
     */
    @Operation(
        summary = "Listar usuários ativos",
        description = "Retorna lista de usuários com status ativo no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários ativos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users/active\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar usuários ativos\",\"path\":\"/api/users/active\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users/active\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/active")
    public ResponseEntity<List<UserDto>> getAllActiveUsers() {
        List<UserDto> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Obtém detalhes de um usuário específico.
     * 
     * @param id ID do usuário
     * @return UserDto dados do usuário
     */
    @Operation(
        summary = "Obter usuário por ID",
        description = "Retorna os dados completos de um usuário específico pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"ID do usuário deve ser um número positivo\",\"path\":\"/api/users/abc\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar este usuário\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/users/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID do usuário", required = true, example = "1") 
            @PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não foi encontrado"));
    }
    
    /**
     * Atualiza dados de um usuário.
     * 
     * @param id ID do usuário
     * @param userDto dados atualizados do usuário
     * @return UserDto usuário atualizado
     */
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário existente incluindo nome, telefone e valor da hora"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"Nome completo é obrigatório e deve ter entre 2 e 100 caracteres\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para atualizar este usuário\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/users/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Email já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Email já existe\",\"mensagem\":\"Este email já está cadastrado no sistema\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID do usuário", required = true, example = "1") 
            @PathVariable Long id, 
            @Parameter(description = "Dados atualizados do usuário", required = true) 
            @Valid @RequestBody UserRegistrationDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Desativa uma conta de usuário.
     * 
     * @param id ID do usuário
     */
    @Operation(
        summary = "Desativar usuário",
        description = "Desativa uma conta de usuário, impedindo login sem excluir os dados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário desativado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"ID do usuário deve ser um número positivo\",\"path\":\"/api/users/abc/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users/1/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para desativar usuários\",\"path\":\"/api/users/1/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/users/999/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Usuário já desativado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Estado inválido\",\"mensagem\":\"Usuário já está desativado\",\"path\":\"/api/users/1/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users/1/deactivate\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "ID do usuário", required = true, example = "1") 
            @PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Ativa uma conta de usuário.
     * 
     * @param id ID do usuário
     */
    @Operation(
        summary = "Ativar usuário",
        description = "Reativa uma conta de usuário desativada, permitindo login novamente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário ativado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"ID do usuário deve ser um número positivo\",\"path\":\"/api/users/abc/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/users/1/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para ativar usuários\",\"path\":\"/api/users/1/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/users/999/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Usuário já ativado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Estado inválido\",\"mensagem\":\"Usuário já está ativado\",\"path\":\"/api/users/1/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/users/1/activate\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "ID do usuário", required = true, example = "1") 
            @PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }
} 