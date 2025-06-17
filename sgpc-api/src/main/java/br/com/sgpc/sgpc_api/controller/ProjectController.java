package br.com.sgpc.sgpc_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.ProjectCreateDto;
import br.com.sgpc.sgpc_api.dto.ProjectDetailsDto;
import br.com.sgpc.sgpc_api.dto.ProjectSummaryDto;
import br.com.sgpc.sgpc_api.dto.ProjectUpdateDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.entity.Attachment;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.service.FileStorageService;
import br.com.sgpc.sgpc_api.service.ProjectService;
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
 * Controller responsável pelo gerenciamento de projetos.
 * 
 * Este controller fornece endpoints para CRUD completo de projetos,
 * gerenciamento de equipes, anexos e consultas específicas como
 * projetos atrasados, por status, cliente, etc.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Gerenciamento de Projetos", description = "Endpoints para gerenciamento completo de projetos de construção")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria um novo projeto.
     * 
     * @param projectCreateDto dados para criação do projeto
     * @return ResponseEntity com os detalhes do projeto criado
     */
    @PostMapping
    @Operation(
        summary = "Criar novo projeto",
        description = "Cria um novo projeto de construção com os dados fornecidos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"name: não deve estar vazio\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para criar projetos\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Projeto já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Projeto já existe\",\"mensagem\":\"Já existe um projeto cadastrado com o nome: Projeto ABC\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ProjectDetailsDto> createProject(
        @Parameter(description = "Dados do projeto a ser criado", required = true)
        @Valid @RequestBody ProjectCreateDto projectCreateDto) {
            ProjectDetailsDto createdProject = projectService.createProject(projectCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * Lista todos os projetos do sistema.
     * 
     * @return ResponseEntity com lista de projetos resumidos
     */
    @GetMapping
    @Operation(
        summary = "Listar todos os projetos",
        description = "Retorna uma lista resumida de todos os projetos do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar projetos\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> getAllProjects() {
        List<ProjectSummaryDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Busca um projeto específico pelo ID.
     * 
     * @param id ID do projeto
     * @return ResponseEntity com detalhes completos do projeto ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obter projeto por ID",
        description = "Retorna os detalhes completos de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projeto encontrado",
                    content = @Content(schema = @Schema(implementation = ProjectDetailsDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar este projeto\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ProjectDetailsDto> getProjectById(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(project -> ResponseEntity.ok(project))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Atualiza dados de um projeto existente.
     * 
     * @param id ID do projeto a ser atualizado
     * @param projectUpdateDto dados para atualização
     * @return ResponseEntity com projeto atualizado
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar projeto",
        description = "Atualiza os dados de um projeto existente. Apenas campos fornecidos serão atualizados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Dados inválidos\",\"mensagem\":\"name: não deve estar vazio\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para atualizar projetos\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Nome já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":409,\"erro\":\"Projeto já existe\",\"mensagem\":\"Já existe um projeto cadastrado com o nome: Projeto XYZ\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ProjectDetailsDto> updateProject(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long id, 
        @Parameter(description = "Dados para atualização do projeto", required = true)
        @Valid @RequestBody ProjectUpdateDto projectUpdateDto) {
            ProjectDetailsDto updatedProject = projectService.updateProject(id, projectUpdateDto);
            return ResponseEntity.ok(updatedProject);
    }

    /**
     * Remove um projeto do sistema.
     * 
     * @param id ID do projeto a ser removido
     * @return ResponseEntity vazio com status 204
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar projeto",
        description = "Remove um projeto do sistema permanentemente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Projeto deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para deletar projetos\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteProject(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long id) {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
    }

    /**
     * Lista projetos filtrados por status.
     * 
     * @param status status dos projetos
     * @return ResponseEntity com lista de projetos do status especificado
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Listar projetos por status",
        description = "Retorna projetos filtrados por status específico (PLANEJAMENTO, EM_ANDAMENTO, PAUSADO, CONCLUIDO, CANCELADO)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Status de projeto inválido\",\"mensagem\":\"Status INVALID não é válido. Use: PLANEJAMENTO, EM_ANDAMENTO, PAUSADO, CONCLUIDO, CANCELADO\",\"path\":\"/api/projects/status/INVALID\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar projetos\",\"path\":\"/api/projects/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/status/EM_ANDAMENTO\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByStatus(
        @Parameter(description = "Status dos projetos (PLANEJAMENTO, EM_ANDAMENTO, PAUSADO, CONCLUIDO, CANCELADO)", required = true)
        @PathVariable String status) {
        try {
            ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
            List<ProjectSummaryDto> projects = projectService.getProjectsByStatus(projectStatus);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            throw new br.com.sgpc.sgpc_api.exception.InvalidProjectStatusException("Status '" + status + "' não é válido. Use: PLANEJAMENTO, EM_ANDAMENTO, PAUSADO, CONCLUIDO, CANCELADO");
        }
    }

    /**
     * Busca projetos por nome.
     * 
     * @param name nome ou parte do nome do projeto
     * @return ResponseEntity com lista de projetos encontrados
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar projetos por nome",
        description = "Realiza busca textual nos nomes dos projetos (case-insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos encontrados",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Parâmetro inválido\",\"mensagem\":\"O parâmetro 'name' é obrigatório para a busca\",\"path\":\"/api/projects/search\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/search\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para buscar projetos\",\"path\":\"/api/projects/search\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/search\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> searchProjects(
        @Parameter(description = "Nome ou parte do nome do projeto", required = true)
        @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("O parâmetro 'name' é obrigatório para a busca");
        }
        
        List<ProjectSummaryDto> projects = projectService.searchProjectsByName(name);
        return ResponseEntity.ok(projects);
    }

    /**
     * Lista projetos de um usuário específico.
     * 
     * @param userId ID do usuário
     * @return ResponseEntity com lista de projetos do usuário
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Listar projetos de um usuário",
        description = "Retorna projetos onde o usuário é membro da equipe"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos do usuário",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/user/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar projetos de outros usuários\",\"path\":\"/api/projects/user/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Usuário não encontrado\",\"mensagem\":\"Usuário com ID 999 não foi encontrado\",\"path\":\"/api/projects/user/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/user/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByUser(
        @Parameter(description = "ID do usuário", required = true)
        @PathVariable Long userId) {
        // Verificar se o usuário existe
        if (!userRepository.existsById(userId)) {
            throw new br.com.sgpc.sgpc_api.exception.UserNotFoundException("Usuário com ID " + userId + " não foi encontrado");
        }
        
        List<ProjectSummaryDto> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }

    /**
     * Lista projetos atrasados.
     * 
     * @return ResponseEntity com lista de projetos que passaram da data planejada
     */
    @GetMapping("/delayed")
    @Operation(
        summary = "Listar projetos atrasados",
        description = "Retorna projetos que passaram da data planejada de conclusão e ainda não foram finalizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos atrasados",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/delayed\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar projetos atrasados\",\"path\":\"/api/projects/delayed\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/delayed\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> getDelayedProjects() {
        List<ProjectSummaryDto> delayedProjects = projectService.getDelayedProjects();
        return ResponseEntity.ok(delayedProjects);
    }

    /**
     * Adiciona um membro à equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário a ser adicionado
     * @return ResponseEntity com projeto atualizado
     */
    @PostMapping("/{projectId}/team/{userId}")
    @Operation(
        summary = "Adicionar membro à equipe",
        description = "Adiciona um usuário à equipe do projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membro adicionado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Usuário já faz parte da equipe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Usuário já está na equipe\",\"mensagem\":\"O usuário já faz parte da equipe do projeto\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerenciar equipe\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto ou usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ProjectDetailsDto> addTeamMember(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long projectId, 
        @Parameter(description = "ID do usuário", required = true)
        @PathVariable Long userId) {
        ProjectDetailsDto updatedProject = projectService.addTeamMember(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Remove um membro da equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @param userId ID do usuário a ser removido
     * @return ResponseEntity com projeto atualizado
     * @throws RuntimeException se ocorrer erro na operação
     */
    @DeleteMapping("/{projectId}/team/{userId}")
    @Operation(
        summary = "Remover membro da equipe",
        description = "Remove um usuário da equipe do projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membro removido com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = "Usuário não faz parte da equipe",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Usuário não está na equipe\",\"mensagem\":\"O usuário não faz parte da equipe do projeto\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerenciar equipe\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto ou usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/team/2\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ProjectDetailsDto> removeTeamMember(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long projectId, 
        @Parameter(description = "ID do usuário", required = true)
        @PathVariable Long userId) {
        try {
            ProjectDetailsDto updatedProject = projectService.removeTeamMember(projectId, userId);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover membro da equipe: " + e.getMessage());
        }
    }

    /**
     * Lista membros da equipe do projeto.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity com lista de membros da equipe
     */
    @GetMapping("/{projectId}/team")
    @Operation(
        summary = "Listar equipe do projeto",
        description = "Retorna todos os membros da equipe do projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de membros da equipe",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/team\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar equipe\",\"path\":\"/api/projects/1/team\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/team\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/team\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<UserDto>> getProjectTeamMembers(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long projectId) {
        List<UserDto> teamMembers = projectService.getProjectTeamMembers(projectId);
        return ResponseEntity.ok(teamMembers);
    }

    /**
     * Lista anexos de um projeto.
     * 
     * @param projectId ID do projeto
     * @return ResponseEntity com lista de anexos
     */
    @GetMapping("/{projectId}/attachments")
    @Operation(
        summary = "Listar anexos do projeto",
        description = "Retorna todos os anexos de um projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anexos",
                    content = @Content(schema = @Schema(implementation = Attachment.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para visualizar anexos\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<Attachment>> getProjectAttachments(
        @Parameter(description = "ID do projeto", required = true)
        @PathVariable Long projectId) {
        // Verificar se o projeto existe primeiro
        if (projectService.getProjectById(projectId).isEmpty()) {
            throw new RuntimeException("Projeto com ID " + projectId + " não foi encontrado");
        }
        
             List<Attachment> attachments = fileStorageService.getAttachmentsByEntity("Project", projectId);
             return ResponseEntity.ok(attachments);
    }

    /**
     * Remove um anexo do projeto.
     * 
     * @param attachmentId ID do anexo
     * @return ResponseEntity vazio com status 204
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(
        summary = "Deletar anexo",
        description = "Remove um anexo do projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Anexo deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para deletar anexos\",\"path\":\"/api/projects/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Anexo não encontrado\",\"mensagem\":\"Anexo com ID 999 não foi encontrado\",\"path\":\"/api/projects/attachments/999\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/attachments/1\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteAttachment(
        @Parameter(description = "ID do anexo", required = true)
        @PathVariable Long attachmentId) {
        try {
            fileStorageService.deleteFile(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e.getMessage().contains("não encontrado")) {
                throw new RuntimeException("Anexo com ID " + attachmentId + " não foi encontrado");
            }
            throw new RuntimeException("Erro ao deletar anexo: " + e.getMessage());
        }
    }

    /**
     * Lista projetos de um cliente específico.
     * 
     * @param client nome do cliente
     * @return ResponseEntity com lista de projetos do cliente
     */
    @GetMapping("/client/{client}")
    @Operation(
        summary = "Listar projetos por cliente",
        description = "Retorna todos os projetos de um cliente específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos do cliente",
                    content = @Content(schema = @Schema(implementation = ProjectSummaryDto.class))),
        @ApiResponse(responseCode = "400", description = "Nome do cliente inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Parâmetro inválido\",\"mensagem\":\"Nome do cliente não pode estar vazio\",\"path\":\"/api/projects/client/\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/client/João Silva\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para listar projetos por cliente\",\"path\":\"/api/projects/client/João Silva\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/client/João Silva\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByClient(
        @Parameter(description = "Nome do cliente", required = true)
        @PathVariable String client) {
        if (client == null || client.trim().isEmpty()) {
            throw new RuntimeException("Nome do cliente não pode estar vazio");
        }
        
        List<ProjectSummaryDto> projects = projectService.getProjectsByClient(client.trim());
        return ResponseEntity.ok(projects);
    }

    /**
     * Faz upload de um anexo para o projeto.
     * 
     * @param projectId ID do projeto
     * @param file arquivo a ser anexado
     * @param authentication dados do usuário autenticado
     * @return ResponseEntity com dados do anexo criado
     */
    @PostMapping("/{projectId}/attachments")
    @Operation(
        summary = "Upload de anexo",
        description = "Faz upload de um arquivo como anexo do projeto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Anexo enviado com sucesso",
                    content = @Content(schema = @Schema(implementation = Attachment.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Arquivo inválido\",\"mensagem\":\"Arquivo muito grande. Tamanho máximo permitido: 10MB\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para enviar anexos\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Projeto não encontrado\",\"mensagem\":\"Projeto com ID 999 não foi encontrado\",\"path\":\"/api/projects/999/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/1/attachments\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Attachment> uploadAttachment(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "Arquivo a ser anexado", required = true)
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        // Verificar se o projeto existe primeiro
        if (projectService.getProjectById(projectId).isEmpty()) {
            throw new br.com.sgpc.sgpc_api.exception.ProjectNotFoundException("Projeto com ID " + projectId + " não foi encontrado");
        }

        // Validar arquivo
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo não pode estar vazio");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new br.com.sgpc.sgpc_api.exception.UserNotFoundException("Usuário não encontrado"));

        Attachment attachment = fileStorageService.storeFile(file, "Project", projectId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
    }

    /**
     * Faz download de um anexo.
     * 
     * @param attachmentId ID do anexo
     * @return ResponseEntity com arquivo para download
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(
        summary = "Download de anexo",
        description = "Faz download de um anexo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo para download"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/projects/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para baixar anexos\",\"path\":\"/api/projects/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":404,\"erro\":\"Anexo não encontrado\",\"mensagem\":\"Anexo com ID 999 não foi encontrado\",\"path\":\"/api/projects/attachments/999/download\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/projects/attachments/1/download\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Resource> downloadAttachment(
        @Parameter(description = "ID do anexo", required = true)
        @PathVariable Long attachmentId) throws IOException {
        Resource resource = fileStorageService.loadFileAsResource(attachmentId);
        Attachment attachment = fileStorageService.getAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(resource);
    }
} 