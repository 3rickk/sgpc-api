package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.MaterialRequestApprovalDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestDetailsDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestSummaryDto;
import br.com.sgpc.sgpc_api.enums.RequestStatus;
import br.com.sgpc.sgpc_api.service.MaterialRequestService;
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
 * Controller responsável pelo gerenciamento de solicitações de materiais.
 * 
 * Este controller fornece endpoints para criação, aprovação, rejeição
 * e consulta de solicitações de materiais para projetos.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/material-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Solicitações de Materiais", description = "Endpoints para gestão de solicitações de materiais")
@SecurityRequirement(name = "bearerAuth")
public class MaterialRequestController {

    @Autowired
    private MaterialRequestService materialRequestService;

    /**
     * Cria uma nova solicitação de material.
     * 
     * @param requestDto dados da solicitação
     * @param requesterId ID do usuário solicitante
     * @return MaterialRequestDetailsDto dados da solicitação criada
     */
    @Operation(
        summary = "Criar solicitação de material",
        description = "Cria uma nova solicitação de material para um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou projeto/usuário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<MaterialRequestDetailsDto> createMaterialRequest(
            @Valid @RequestBody @Parameter(description = "Dados da solicitação de material") MaterialRequestCreateDto requestDto,
            @RequestParam @Parameter(description = "ID do usuário solicitante", example = "1") Long requesterId) {
        try {
            MaterialRequestDetailsDto createdRequest = materialRequestService.createMaterialRequest(requestDto, requesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todas as solicitações de material com filtros opcionais.
     * 
     * @param status status da solicitação (opcional)
     * @param projectId ID do projeto (opcional)
     * @return List<MaterialRequestSummaryDto> lista de solicitações
     */
    @Operation(
        summary = "Listar solicitações de material",
        description = "Lista todas as solicitações de material com filtros opcionais por status ou projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<MaterialRequestSummaryDto>> getAllMaterialRequests(
            @RequestParam(required = false) @Parameter(description = "Status da solicitação", example = "PENDENTE") String status,
            @RequestParam(required = false) @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        try {
            List<MaterialRequestSummaryDto> requests;
            
            if (status != null) {
                RequestStatus requestStatus = RequestStatus.fromString(status);
                requests = materialRequestService.getMaterialRequestsByStatus(requestStatus);
            } else if (projectId != null) {
                requests = materialRequestService.getMaterialRequestsByProject(projectId);
            } else {
                requests = materialRequestService.getAllMaterialRequests();
            }
            
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtém detalhes de uma solicitação específica.
     * 
     * @param id ID da solicitação
     * @return MaterialRequestDetailsDto detalhes da solicitação
     */
    @Operation(
        summary = "Obter detalhes da solicitação",
        description = "Retorna os detalhes completos de uma solicitação de material específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalhes da solicitação retornados com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MaterialRequestDetailsDto> getMaterialRequestById(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id) {
        return materialRequestService.getMaterialRequestById(id)
                .map(request -> ResponseEntity.ok(request))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aprova uma solicitação de material.
     * 
     * @param id ID da solicitação
     * @param approverId ID do usuário aprovador
     * @return MaterialRequestDetailsDto solicitação aprovada
     */
    @Operation(
        summary = "Aprovar solicitação",
        description = "Aprova uma solicitação de material pendente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitação aprovada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitação não pode ser aprovada ou usuário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<MaterialRequestDetailsDto> approveMaterialRequest(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário aprovador", example = "1") Long approverId) {
        try {
            MaterialRequestDetailsDto approvedRequest = materialRequestService.approveMaterialRequest(id, approverId);
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Rejeita uma solicitação de material.
     * 
     * @param id ID da solicitação
     * @param approverId ID do usuário que rejeitou
     * @param approvalDto dados da rejeição
     * @return MaterialRequestDetailsDto solicitação rejeitada
     */
    @Operation(
        summary = "Rejeitar solicitação",
        description = "Rejeita uma solicitação de material pendente com justificativa"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitação rejeitada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitação não pode ser rejeitada ou dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<MaterialRequestDetailsDto> rejectMaterialRequest(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que rejeitou", example = "1") Long approverId,
            @Valid @RequestBody @Parameter(description = "Dados da rejeição") MaterialRequestApprovalDto approvalDto) {
        try {
            MaterialRequestDetailsDto rejectedRequest = materialRequestService.rejectMaterialRequest(id, approverId, approvalDto);
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista solicitações pendentes.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações pendentes
     */
    @Operation(
        summary = "Listar solicitações pendentes",
        description = "Retorna todas as solicitações de material com status pendente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações pendentes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getPendingMaterialRequests() {
        List<MaterialRequestSummaryDto> pendingRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.PENDENTE);
        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Lista solicitações aprovadas.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações aprovadas
     */
    @Operation(
        summary = "Listar solicitações aprovadas",
        description = "Retorna todas as solicitações de material com status aprovado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações aprovadas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/approved")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getApprovedMaterialRequests() {
        List<MaterialRequestSummaryDto> approvedRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.APROVADA);
        return ResponseEntity.ok(approvedRequests);
    }

    /**
     * Lista solicitações rejeitadas.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações rejeitadas
     */
    @Operation(
        summary = "Listar solicitações rejeitadas",
        description = "Retorna todas as solicitações de material com status rejeitado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações rejeitadas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/rejected")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getRejectedMaterialRequests() {
        List<MaterialRequestSummaryDto> rejectedRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.REJEITADA);
        return ResponseEntity.ok(rejectedRequests);
    }

    /**
     * Lista solicitações de um projeto específico.
     * 
     * @param projectId ID do projeto
     * @return List<MaterialRequestSummaryDto> lista de solicitações do projeto
     */
    @Operation(
        summary = "Listar solicitações por projeto",
        description = "Retorna todas as solicitações de material de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações do projeto retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getMaterialRequestsByProject(
            @PathVariable @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        List<MaterialRequestSummaryDto> projectRequests = 
                materialRequestService.getMaterialRequestsByProject(projectId);
        return ResponseEntity.ok(projectRequests);
    }
} 