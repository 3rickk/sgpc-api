package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.sgpc.sgpc_api.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para detalhes completos de uma solicitação de materiais.
 * 
 * Contém informações completas sobre uma solicitação incluindo:
 * - Dados do solicitante e projeto
 * - Status e workflow de aprovação
 * - Lista detalhada de itens solicitados
 * - Valores financeiros calculados
 * - Histórico de modificações
 * 
 * Usado para:
 * - Visualização completa da solicitação
 * - Processo de aprovação/rejeição
 * - Acompanhamento do status
 * - Auditoria do processo
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhes completos de uma solicitação de materiais")
public class MaterialRequestDetailsDto {

    @Schema(description = "ID único da solicitação", example = "1")
    private Long id;

    @Schema(description = "ID do projeto", example = "1")
    private Long projectId;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String projectName;

    @Schema(description = "ID do usuário solicitante", example = "2")
    private Long requesterId;

    @Schema(description = "Nome do solicitante", example = "Carlos Silva")
    private String requesterName;

    @Schema(description = "Data da solicitação", example = "2024-03-15")
    private LocalDate requestDate;

    @Schema(description = "Data necessária para entrega", example = "2024-03-20")
    private LocalDate neededDate;

    @Schema(description = "Status atual da solicitação")
    private RequestStatus status;

    @Schema(description = "Descrição textual do status", example = "Pendente de Aprovação")
    private String statusDescription;

    @Schema(description = "Motivo da rejeição (se aplicável)", 
            example = "Quantidade excessiva para o orçamento do projeto")
    private String rejectionReason;

    @Schema(description = "ID do usuário que aprovou", example = "1")
    private Long approvedById;

    @Schema(description = "Nome do aprovador", example = "Ana Santos")
    private String approvedByName;

    @Schema(description = "Data e hora da aprovação", example = "2024-03-16T10:30:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Observações gerais da solicitação", 
            example = "Materiais urgentes para conclusão da fundação")
    private String observations;

    @Schema(description = "Data de criação", example = "2024-03-15T08:45:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-03-16T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Lista de itens solicitados")
    private List<MaterialRequestItemDto> items;

    @Schema(description = "Valor total da solicitação", example = "3450.00")
    private BigDecimal totalAmount;
} 