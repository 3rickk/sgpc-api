package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestApprovalDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestDetailsDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestSummaryDto;
import br.com.sgpc.sgpc_api.enums.RequestStatus;
import br.com.sgpc.sgpc_api.service.MaterialRequestService;
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
 * Controller responsável pelo gerenciamento de solicitações de materiais.
 * 
 * Este controller fornece endpoints para criação, aprovação, rejeição
 * e consulta de solicitações de materiais para projetos.
 * 
 * Permissões:
 * - ADMIN: Acesso completo a todos os endpoints
 * - MANAGER: Pode aprovar/rejeitar solicitações e visualizar todas
 * - USER: Pode criar solicitações e visualizar as próprias
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/material-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Solicitações de Materiais", description = "Endpoints para gestão de solicitações de materiais")
@SecurityRequirement(name = "Bearer Authentication")
public class MaterialRequestController {

    @Autowired
    private MaterialRequestService materialRequestService;

    /**
     * Cria uma nova solicitação de material.
     * Todos os usuários autenticados podem criar solicitações.
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
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class),
                examples = @ExampleObject(
                    name = "Solicitação criada",
                    summary = "Nova solicitação de material",
                    value = "{\"id\":1,\"projectId\":1,\"projectName\":\"Construção Alpha\",\"requesterId\":1,\"requesterName\":\"João Silva\",\"status\":\"PENDENTE\",\"items\":[{\"materialId\":1,\"materialName\":\"Cimento\",\"quantityRequested\":100.0}],\"requestDate\":\"2024-01-15T10:30:00\"}"
                ))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"Quantidade solicitada deve ser maior que zero\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para criar solicitações\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Não encontrado\",\"mensagem\":\"Projeto com ID 1 não foi encontrado\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<MaterialRequestDetailsDto> createMaterialRequest(
            @Valid @RequestBody @Parameter(description = "Dados da solicitação de material") MaterialRequestCreateDto requestDto,
            @RequestParam @Parameter(description = "ID do usuário solicitante", example = "1") Long requesterId) {
            MaterialRequestDetailsDto createdRequest = materialRequestService.createMaterialRequest(requestDto, requesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    /**
     * Lista todas as solicitações de material com filtros opcionais.
     * ADMIN e MANAGER podem ver todas as solicitações.
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
        @ApiResponse(responseCode = "200", description = "Lista de solicitações retornada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestSummaryDto.class),
                examples = @ExampleObject(
                    name = "Lista de solicitações",
                    summary = "Solicitações de materiais",
                    value = "[{\"id\":1,\"projectName\":\"Construção Alpha\",\"requesterName\":\"João Silva\",\"status\":\"PENDENTE\",\"itemsCount\":3,\"requestDate\":\"2024-01-15T10:30:00\"},{\"id\":2,\"projectName\":\"Reforma Beta\",\"requesterName\":\"Maria Santos\",\"status\":\"APROVADA\",\"itemsCount\":1,\"requestDate\":\"2024-01-14T15:45:00\"}]"
                ))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Parâmetro inválido\",\"mensagem\":\"Status 'INVALIDO' não é válido. Use: PENDENTE, APROVADA, REJEITADA\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar solicitações\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getAllMaterialRequests(
            @RequestParam(required = false) @Parameter(description = "Status da solicitação", example = "PENDENTE") String status,
            @RequestParam(required = false) @Parameter(description = "ID do projeto", example = "1") Long projectId) {
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
    }

    /**
     * Obtém detalhes de uma solicitação específica.
     * ADMIN e MANAGER podem ver qualquer solicitação.
     * USER pode ver apenas suas próprias solicitações.
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
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class),
                examples = @ExampleObject(
                    name = "Detalhes da solicitação",
                    summary = "Solicitação completa",
                    value = "{\"id\":1,\"projectId\":1,\"projectName\":\"Construção Alpha\",\"requesterId\":1,\"requesterName\":\"João Silva\",\"status\":\"PENDENTE\",\"items\":[{\"materialId\":1,\"materialName\":\"Cimento\",\"quantityRequested\":100.0,\"unit\":\"kg\"},{\"materialId\":2,\"materialName\":\"Areia\",\"quantityRequested\":5.0,\"unit\":\"m³\"}],\"requestDate\":\"2024-01-15T10:30:00\",\"notes\":\"Materiais para fundação\"}"
                ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar esta solicitação\",\"path\":\"/api/material-requests/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Não encontrado\",\"mensagem\":\"Solicitação com ID 1 não foi encontrada\",\"path\":\"/api/material-requests/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @materialRequestService.isRequestOwner(#id, authentication.principal.id))")
    public ResponseEntity<MaterialRequestDetailsDto> getMaterialRequestById(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id) {
        return materialRequestService.getMaterialRequestById(id)
                .map(request -> ResponseEntity.ok(request))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aprova uma solicitação de material.
     * Apenas ADMIN e MANAGER podem aprovar solicitações.
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
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class),
                examples = @ExampleObject(
                    name = "Solicitação aprovada",
                    summary = "Status atualizado para aprovada",
                    value = "{\"id\":1,\"projectId\":1,\"projectName\":\"Construção Alpha\",\"status\":\"APROVADA\",\"approverId\":2,\"approverName\":\"Ana Gerente\",\"approvalDate\":\"2024-01-15T14:30:00\",\"items\":[{\"materialId\":1,\"materialName\":\"Cimento\",\"quantityRequested\":100.0}]}"
                ))),
        @ApiResponse(responseCode = "400", description = "Solicitação não pode ser aprovada",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Operação inválida\",\"mensagem\":\"Solicitação já foi processada e não pode ser aprovada\",\"path\":\"/api/material-requests/1/approve\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/1/approve\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para aprovar solicitações\",\"path\":\"/api/material-requests/1/approve\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Não encontrado\",\"mensagem\":\"Solicitação com ID 1 não foi encontrada\",\"path\":\"/api/material-requests/1/approve\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/1/approve\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MaterialRequestDetailsDto> approveMaterialRequest(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário aprovador", example = "1") Long approverId) {
        MaterialRequestDetailsDto approvedRequest = materialRequestService.approveMaterialRequest(id, approverId);
        return ResponseEntity.ok(approvedRequest);
    }

    /**
     * Rejeita uma solicitação de material.
     * Apenas ADMIN e MANAGER podem rejeitar solicitações.
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
                content = @Content(schema = @Schema(implementation = MaterialRequestDetailsDto.class),
                examples = @ExampleObject(
                    name = "Solicitação rejeitada",
                    summary = "Status atualizado para rejeitada",
                    value = "{\"id\":1,\"projectId\":1,\"projectName\":\"Construção Alpha\",\"status\":\"REJEITADA\",\"approverId\":2,\"approverName\":\"Ana Gerente\",\"approvalDate\":\"2024-01-15T14:30:00\",\"rejectionReason\":\"Orçamento insuficiente\",\"items\":[{\"materialId\":1,\"materialName\":\"Cimento\",\"quantityRequested\":100.0}]}"
                ))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou solicitação não pode ser rejeitada",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Operação inválida\",\"mensagem\":\"Justificativa da rejeição é obrigatória\",\"path\":\"/api/material-requests/1/reject\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/1/reject\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para rejeitar solicitações\",\"path\":\"/api/material-requests/1/reject\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Não encontrado\",\"mensagem\":\"Solicitação com ID 1 não foi encontrada\",\"path\":\"/api/material-requests/1/reject\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/1/reject\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MaterialRequestDetailsDto> rejectMaterialRequest(
            @PathVariable @Parameter(description = "ID da solicitação", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que rejeitou", example = "1") Long approverId,
            @Valid @RequestBody @Parameter(description = "Dados da rejeição") MaterialRequestApprovalDto approvalDto) {
        MaterialRequestDetailsDto rejectedRequest = materialRequestService.rejectMaterialRequest(id, approverId, approvalDto);
        return ResponseEntity.ok(rejectedRequest);
    }

    /**
     * Lista solicitações pendentes.
     * ADMIN e MANAGER podem visualizar solicitações pendentes.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações pendentes
     */
    @Operation(
        summary = "Listar solicitações pendentes",
        description = "Retorna todas as solicitações de material com status pendente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações pendentes retornada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestSummaryDto.class),
                examples = @ExampleObject(
                    name = "Solicitações pendentes",
                    summary = "Lista de solicitações aguardando aprovação",
                    value = "[{\"id\":1,\"projectName\":\"Construção Alpha\",\"requesterName\":\"João Silva\",\"status\":\"PENDENTE\",\"itemsCount\":3,\"requestDate\":\"2024-01-15T10:30:00\"},{\"id\":3,\"projectName\":\"Reforma Gama\",\"requesterName\":\"Carlos Pereira\",\"status\":\"PENDENTE\",\"itemsCount\":2,\"requestDate\":\"2024-01-14T09:15:00\"}]"
                ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/pending\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar solicitações pendentes\",\"path\":\"/api/material-requests/pending\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/pending\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getPendingMaterialRequests() {
        List<MaterialRequestSummaryDto> pendingRequests = materialRequestService.getMaterialRequestsByStatus(RequestStatus.PENDENTE);
        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Lista solicitações aprovadas.
     * ADMIN e MANAGER podem visualizar solicitações aprovadas.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações aprovadas
     */
    @Operation(
        summary = "Listar solicitações aprovadas",
        description = "Retorna todas as solicitações de material com status aprovado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações aprovadas retornada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestSummaryDto.class),
                examples = @ExampleObject(
                    name = "Solicitações aprovadas",
                    summary = "Lista de solicitações já aprovadas",
                    value = "[{\"id\":2,\"projectName\":\"Reforma Beta\",\"requesterName\":\"Maria Santos\",\"status\":\"APROVADA\",\"approverName\":\"Ana Gerente\",\"itemsCount\":1,\"requestDate\":\"2024-01-14T15:45:00\",\"approvalDate\":\"2024-01-14T16:30:00\"},{\"id\":4,\"projectName\":\"Construção Delta\",\"requesterName\":\"Pedro Lima\",\"status\":\"APROVADA\",\"approverName\":\"Carlos Supervisor\",\"itemsCount\":5,\"requestDate\":\"2024-01-13T11:20:00\",\"approvalDate\":\"2024-01-13T14:10:00\"}]"
                ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/approved\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar solicitações aprovadas\",\"path\":\"/api/material-requests/approved\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/approved\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/approved")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getApprovedMaterialRequests() {
        List<MaterialRequestSummaryDto> approvedRequests = materialRequestService.getMaterialRequestsByStatus(RequestStatus.APROVADA);
        return ResponseEntity.ok(approvedRequests);
    }

    /**
     * Lista solicitações rejeitadas.
     * ADMIN e MANAGER podem visualizar solicitações rejeitadas.
     * 
     * @return List<MaterialRequestSummaryDto> lista de solicitações rejeitadas
     */
    @Operation(
        summary = "Listar solicitações rejeitadas",
        description = "Retorna todas as solicitações de material com status rejeitado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações rejeitadas retornada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestSummaryDto.class),
                examples = @ExampleObject(
                    name = "Solicitações rejeitadas",
                    summary = "Lista de solicitações rejeitadas",
                    value = "[{\"id\":5,\"projectName\":\"Expansão Epsilon\",\"requesterName\":\"Ana Costa\",\"status\":\"REJEITADA\",\"approverName\":\"Roberto Diretor\",\"itemsCount\":2,\"requestDate\":\"2024-01-12T14:30:00\",\"approvalDate\":\"2024-01-12T17:45:00\",\"rejectionReason\":\"Orçamento insuficiente para este trimestre\"}]"
                ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/rejected\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar solicitações rejeitadas\",\"path\":\"/api/material-requests/rejected\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/rejected\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/rejected")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getRejectedMaterialRequests() {
        List<MaterialRequestSummaryDto> rejectedRequests = materialRequestService.getMaterialRequestsByStatus(RequestStatus.REJEITADA);
        return ResponseEntity.ok(rejectedRequests);
    }

    /**
     * Lista solicitações por projeto.
     * ADMIN e MANAGER podem ver solicitações de qualquer projeto.
     * USER pode ver apenas solicitações de projetos que participa.
     * 
     * @param projectId ID do projeto
     * @return List<MaterialRequestSummaryDto> lista de solicitações do projeto
     */
    @Operation(
        summary = "Listar solicitações por projeto",
        description = "Retorna todas as solicitações de material de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações do projeto retornada com sucesso",
                content = @Content(schema = @Schema(implementation = MaterialRequestSummaryDto.class),
                examples = @ExampleObject(
                    name = "Solicitações do projeto",
                    summary = "Todas as solicitações de um projeto",
                    value = "[{\"id\":1,\"projectName\":\"Construção Alpha\",\"requesterName\":\"João Silva\",\"status\":\"PENDENTE\",\"itemsCount\":3,\"requestDate\":\"2024-01-15T10:30:00\"},{\"id\":6,\"projectName\":\"Construção Alpha\",\"requesterName\":\"Maria Santos\",\"status\":\"APROVADA\",\"approverName\":\"Ana Gerente\",\"itemsCount\":2,\"requestDate\":\"2024-01-14T08:45:00\",\"approvalDate\":\"2024-01-14T10:20:00\"}]"
                ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/material-requests/project/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar solicitações deste projeto\",\"path\":\"/api/material-requests/project/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Não encontrado\",\"mensagem\":\"Projeto com ID 1 não foi encontrado\",\"path\":\"/api/material-requests/project/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/material-requests/project/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @projectService.isUserInProjectTeam(#projectId, authentication.principal.id))")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getMaterialRequestsByProject(
            @PathVariable @Parameter(description = "ID do projeto", example = "1") Long projectId) {
        List<MaterialRequestSummaryDto> projectRequests = materialRequestService.getMaterialRequestsByProject(projectId);
        return ResponseEntity.ok(projectRequests);
    }
}