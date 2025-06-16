package br.com.sgpc.sgpc_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rejeição de solicitação de materiais.
 * 
 * Usado quando um aprovador rejeita uma solicitação de materiais,
 * fornecendo o motivo da rejeição que será registrado no sistema
 * e comunicado ao solicitante.
 * 
 * Workflow de rejeição:
 * 1. Aprovador analisa a solicitação
 * 2. Decide rejeitar fornecendo motivo claro
 * 3. Sistema atualiza status para REJEITADA
 * 4. Solicitante é notificado do motivo
 * 5. Solicitante pode criar nova solicitação corrigida
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para rejeição de uma solicitação de materiais")
public class MaterialRequestApprovalDto {
    
    @NotBlank(message = "Motivo da rejeição é obrigatório")
    @Schema(description = "Motivo detalhado da rejeição da solicitação", 
            example = "Quantidade excessiva para o orçamento atual do projeto. Reduzir para 30 sacos de cimento.",
            required = true)
    private String rejectionReason;
} 