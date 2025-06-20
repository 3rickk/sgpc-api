package br.com.sgpc.sgpc_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.DashboardDto;
import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller responsável pelos dados do dashboard principal.
 * 
 * Este controller fornece endpoint para obtenção de dados
 * consolidados para exibição no dashboard, incluindo
 * estatísticas e resumos dos principais módulos do sistema.
 * 
 * Permissões:
 * - ADMIN: Acesso completo aos dados do dashboard
 * - MANAGER: Acesso completo aos dados do dashboard
 * - USER: Acesso limitado aos dados do dashboard baseado nos projetos que participa
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Dashboard", description = "Endpoint para dados do dashboard principal")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Obtém dados consolidados para o dashboard.
     * Todos os usuários autenticados podem acessar o dashboard.
     * 
     * Retorna estatísticas e resumos de projetos, tarefas, materiais
     * e outras informações relevantes para exibição no dashboard principal.
     * 
     * @return DashboardDto dados consolidados do dashboard
     */
    @Operation(
        summary = "Obter dados do dashboard",
        description = "Retorna dados consolidados incluindo estatísticas de projetos, tarefas, materiais e outras métricas importantes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do dashboard obtidos com sucesso",
                content = @Content(schema = @Schema(implementation = DashboardDto.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(
                        value = "{\"status\": 401, \"erro\": \"Token expirado\", \"mensagem\": \"Sua sessão expirou. Faça login novamente\", \"path\": \"/api/dashboard\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )
                )),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(
                        value = "{\"status\": 403, \"erro\": \"Acesso negado\", \"mensagem\": \"Você não tem permissão para acessar os dados do dashboard\", \"path\": \"/api/dashboard\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )
                )),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(
                        value = "{\"status\": 500, \"erro\": \"Erro no carregamento do dashboard\", \"mensagem\": \"Erro ao carregar dados do dashboard: Falha na conexão com banco de dados\", \"path\": \"/api/dashboard\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )
                ))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<DashboardDto> getDashboardData() {
        DashboardDto dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
} 