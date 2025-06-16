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

import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@SecurityRequirement(name = "bearerAuth")
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
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão")
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
        @ApiResponse(responseCode = "200", description = "Lista de usuários ativos retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão")
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
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable @Parameter(description = "ID do usuário", example = "1") Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
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
        description = "Atualiza os dados de um usuário existente incluindo nome, email e funções"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já utilizado"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable @Parameter(description = "ID do usuário", example = "1") Long id, 
            @Valid @RequestBody @Parameter(description = "Dados atualizados do usuário") UserRegistrationDto userDto) {
        try {
            UserDto updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage());
        }
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
        @ApiResponse(responseCode = "400", description = "Erro ao desativar usuário"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable @Parameter(description = "ID do usuário", example = "1") Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desativar usuário: " + e.getMessage());
        }
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
        @ApiResponse(responseCode = "400", description = "Erro ao ativar usuário"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário sem permissão"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable @Parameter(description = "ID do usuário", example = "1") Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ativar usuário: " + e.getMessage());
        }
    }
} 