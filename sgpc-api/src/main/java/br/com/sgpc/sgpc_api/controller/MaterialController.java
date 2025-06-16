package br.com.sgpc.sgpc_api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import br.com.sgpc.sgpc_api.dto.MaterialCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialDto;
import br.com.sgpc.sgpc_api.dto.MaterialUpdateDto;
import br.com.sgpc.sgpc_api.dto.StockMovementDto;
import br.com.sgpc.sgpc_api.service.MaterialService;
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
 * Controller responsável pelo gerenciamento de materiais e controle de estoque.
 * 
 * Este controller fornece endpoints para CRUD completo de materiais,
 * controle de estoque, pesquisas e relatórios de materiais com baixo estoque.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Gerenciamento de Materiais", description = "Endpoints para gerenciamento de materiais e controle de estoque")
@SecurityRequirement(name = "Bearer Authentication")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * Cria um novo material no sistema.
     * 
     * @param materialCreateDto dados do material a ser criado
     * @return ResponseEntity contendo os dados do material criado
     * @throws RuntimeException se ocorrer erro na criação
     */
    @PostMapping
    @Operation(
        summary = "Criar novo material",
        description = "Cria um novo material no sistema com informações de estoque e fornecedor"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Material criado com sucesso",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "409", description = "Material já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MaterialDto> createMaterial(
            @Parameter(description = "Dados do material a ser criado", required = true)
            @Valid @RequestBody MaterialCreateDto materialCreateDto) {
        try {
            MaterialDto createdMaterial = materialService.createMaterial(materialCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar material: " + e.getMessage());
        }
    }

    /**
     * Obtém todos os materiais cadastrados.
     * 
     * @return ResponseEntity contendo lista de todos os materiais
     */
    @GetMapping
    @Operation(
        summary = "Listar todos os materiais",
        description = "Obtém lista completa de todos os materiais cadastrados no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de materiais obtida com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<MaterialDto>> getAllMaterials() {
        List<MaterialDto> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(materials);
    }

    /**
     * Obtém um material específico por ID.
     * 
     * @param id ID do material
     * @return ResponseEntity contendo os dados do material ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obter material por ID",
        description = "Obtém os detalhes de um material específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material encontrado",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado")
    })
    public ResponseEntity<MaterialDto> getMaterialById(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id) {
        return materialService.getMaterialById(id)
                .map(material -> ResponseEntity.ok(material))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Atualiza um material existente.
     * 
     * @param id ID do material a ser atualizado
     * @param materialUpdateDto dados para atualização do material
     * @return ResponseEntity contendo os dados atualizados do material
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar material",
        description = "Atualiza os dados de um material existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MaterialDto> updateMaterial(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados para atualização do material", required = true)
            @Valid @RequestBody MaterialUpdateDto materialUpdateDto) {
        try {
            MaterialDto updatedMaterial = materialService.updateMaterial(id, materialUpdateDto);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar material: " + e.getMessage());
        }
    }

    /**
     * Exclui um material do sistema.
     * 
     * @param id ID do material a ser excluído
     * @return ResponseEntity sem conteúdo (204)
     * @throws RuntimeException se ocorrer erro na exclusão
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir material",
        description = "Remove um material do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Material excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "409", description = "Material em uso e não pode ser excluído"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> deleteMaterial(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar material: " + e.getMessage());
        }
    }

    /**
     * Pesquisa materiais por nome.
     * 
     * @param name nome do material para pesquisa (busca parcial)
     * @return ResponseEntity contendo lista de materiais encontrados
     */
    @GetMapping("/search")
    @Operation(
        summary = "Pesquisar materiais por nome",
        description = "Busca materiais que contenham o nome especificado (busca case-insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de materiais encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<MaterialDto>> searchMaterials(
            @Parameter(description = "Nome do material para pesquisa", required = true)
            @RequestParam String name) {
        List<MaterialDto> materials = materialService.searchMaterialsByName(name);
        return ResponseEntity.ok(materials);
    }

    /**
     * Obtém materiais de um fornecedor específico.
     * 
     * @param supplier nome do fornecedor
     * @return ResponseEntity contendo lista de materiais do fornecedor
     */
    @GetMapping("/supplier/{supplier}")
    @Operation(
        summary = "Obter materiais por fornecedor",
        description = "Lista todos os materiais de um fornecedor específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de materiais do fornecedor"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<MaterialDto>> getMaterialsBySupplier(
            @Parameter(description = "Nome do fornecedor", required = true)
            @PathVariable String supplier) {
        List<MaterialDto> materials = materialService.getMaterialsBySupplier(supplier);
        return ResponseEntity.ok(materials);
    }

    /**
     * Obtém materiais com estoque baixo.
     * 
     * Retorna materiais que estão com quantidade atual abaixo
     * do estoque mínimo definido.
     * 
     * @return ResponseEntity contendo lista de materiais com estoque baixo
     */
    @GetMapping("/low-stock")
    @Operation(
        summary = "Obter materiais com estoque baixo",
        description = "Lista materiais que estão com quantidade atual abaixo do estoque mínimo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de materiais com estoque baixo"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<MaterialDto>> getMaterialsBelowMinimumStock() {
        List<MaterialDto> materials = materialService.getMaterialsBelowMinimumStock();
        return ResponseEntity.ok(materials);
    }

    // Endpoints para controle de estoque
    
    /**
     * Atualiza o estoque de um material.
     * 
     * Permite entrada ou saída de material com registro de movimentação.
     * 
     * @param id ID do material
     * @param stockMovementDto dados da movimentação de estoque
     * @return ResponseEntity contendo os dados atualizados do material
     * @throws RuntimeException se ocorrer erro na atualização
     */
    @PostMapping("/{id}/stock")
    @Operation(
        summary = "Atualizar estoque do material",
        description = "Registra movimentação de estoque (entrada ou saída) com histórico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MaterialDto> updateStock(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados da movimentação de estoque", required = true)
            @Valid @RequestBody StockMovementDto stockMovementDto) {
        try {
            MaterialDto updatedMaterial = materialService.updateStock(id, stockMovementDto);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar estoque: " + e.getMessage());
        }
    }

    /**
     * Adiciona quantidade ao estoque.
     * 
     * Endpoint simplificado para entrada de material no estoque.
     * 
     * @param id ID do material
     * @param quantity quantidade a ser adicionada
     * @return ResponseEntity contendo os dados atualizados do material
     * @throws RuntimeException se ocorrer erro na adição
     */
    @PostMapping("/{id}/stock/add")
    @Operation(
        summary = "Adicionar ao estoque",
        description = "Endpoint simplificado para entrada de material no estoque"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quantidade adicionada com sucesso",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MaterialDto> addStock(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id,
            @Parameter(description = "Quantidade a ser adicionada", required = true)
            @RequestParam BigDecimal quantity) {
        try {
            MaterialDto updatedMaterial = materialService.addStock(id, quantity);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar estoque: " + e.getMessage());
        }
    }

    /**
     * Remove quantidade do estoque.
     * 
     * Endpoint simplificado para saída de material do estoque.
     * 
     * @param id ID do material
     * @param quantity quantidade a ser removida
     * @return ResponseEntity contendo os dados atualizados do material
     * @throws RuntimeException se ocorrer erro na remoção ou estoque insuficiente
     */
    @PostMapping("/{id}/stock/remove")
    @Operation(
        summary = "Remover do estoque",
        description = "Endpoint simplificado para saída de material do estoque"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quantidade removida com sucesso",
                    content = @Content(schema = @Schema(implementation = MaterialDto.class))),
        @ApiResponse(responseCode = "400", description = "Quantidade inválida ou estoque insuficiente"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MaterialDto> removeStock(
            @Parameter(description = "ID do material", required = true)
            @PathVariable Long id,
            @Parameter(description = "Quantidade a ser removida", required = true)
            @RequestParam BigDecimal quantity) {
        try {
            MaterialDto updatedMaterial = materialService.removeStock(id, quantity);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover estoque: " + e.getMessage());
        }
    }
} 