package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de solicitações de materiais.
 * 
 * Representa uma nova solicitação de materiais para um projeto específico.
 * Contém a lista de materiais necessários com suas quantidades e
 * informações adicionais como data necessária e observações.
 * 
 * Workflow de solicitação:
 * 1. Usuário cria solicitação com materiais necessários
 * 2. Sistema valida disponibilidade em estoque
 * 3. Solicitação vai para aprovação se necessário
 * 4. Após aprovação, materiais são reservados/entregues
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de uma nova solicitação de materiais")
public class MaterialRequestCreateDto {
    
    @NotNull(message = "ID do projeto é obrigatório")
    @Schema(description = "ID do projeto que solicita os materiais", example = "1", required = true)
    private Long projectId;
    
    @Schema(description = "Data necessária para entrega dos materiais", example = "2024-03-20")
    private LocalDate neededDate;
    
    @Schema(description = "Observações adicionais sobre a solicitação", 
            example = "Materiais urgentes para conclusão da fundação")
    private String observations;
    
    @NotEmpty(message = "Lista de itens não pode estar vazia")
    @Valid
    @Schema(description = "Lista de materiais solicitados com quantidades", required = true)
    private List<MaterialRequestCreateItemDto> items;
} 