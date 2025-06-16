package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resumo de solicitações de materiais.
 * 
 * Contém informações essenciais para listagens de solicitações.
 * Usado em endpoints que retornam múltiplas solicitações onde
 * detalhes completos não são necessários.
 * 
 * Inclui:
 * - Dados básicos da solicitação
 * - Status e prazos
 * - Resumo financeiro (quantidade de itens e valor total)
 * - Informações do solicitante e projeto
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de uma solicitação de materiais")
public class MaterialRequestSummaryDto {

    @Schema(description = "ID único da solicitação", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Construção Residencial Vila Nova")
    private String projectName;

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

    @Schema(description = "Número de itens na solicitação", example = "3")
    private Integer itemCount;

    @Schema(description = "Valor total da solicitação", example = "3450.00")
    private BigDecimal totalAmount;

    @Schema(description = "Data de criação", example = "2024-03-15T08:45:00")
    private LocalDateTime createdAt;
} 