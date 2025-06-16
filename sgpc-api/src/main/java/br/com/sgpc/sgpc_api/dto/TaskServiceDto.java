package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para serviços executados em tarefas.
 * 
 * Representa um serviço específico realizado dentro de uma tarefa,
 * com detalhamento completo dos custos por categoria e cálculos
 * automáticos de totais.
 * 
 * Estrutura de custos:
 * - Custos unitários (mão de obra, material, equipamento)
 * - Quantidade executada
 * - Custos totais calculados (unitário * quantidade)
 * - Possibilidade de override de custo unitário
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Serviço executado em uma tarefa com detalhamento de custos")
public class TaskServiceDto {
    
    @Schema(description = "ID único do serviço da tarefa", example = "1")
    private Long id;

    @Schema(description = "ID da tarefa", example = "1")
    private Long taskId;

    @Schema(description = "Título da tarefa", example = "Escavação da fundação")
    private String taskTitle;

    @Schema(description = "ID do serviço no cadastro", example = "1")
    private Long serviceId;

    @Schema(description = "Nome do serviço", example = "Escavação manual")
    private String serviceName;

    @Schema(description = "Descrição detalhada do serviço", 
            example = "Escavação manual para fundação com profundidade de 1,5m")
    private String serviceDescription;

    @Schema(description = "Unidade de medida", example = "m³")
    private String unitOfMeasurement;

    @Schema(description = "Quantidade executada", example = "25.5")
    private BigDecimal quantity;

    @Schema(description = "Override do custo unitário total (opcional)", example = "120.00")
    private BigDecimal unitCostOverride;

    @Schema(description = "Custo unitário de mão de obra", example = "45.00")
    private BigDecimal unitLaborCost;

    @Schema(description = "Custo unitário de material", example = "25.00")
    private BigDecimal unitMaterialCost;

    @Schema(description = "Custo unitário de equipamento", example = "15.00")
    private BigDecimal unitEquipmentCost;

    @Schema(description = "Custo total de mão de obra (quantidade * unitário)", example = "1147.50")
    private BigDecimal totalLaborCost;

    @Schema(description = "Custo total de material (quantidade * unitário)", example = "637.50")
    private BigDecimal totalMaterialCost;

    @Schema(description = "Custo total de equipamento (quantidade * unitário)", example = "382.50")
    private BigDecimal totalEquipmentCost;

    @Schema(description = "Custo total do serviço", example = "2167.50")
    private BigDecimal totalCost;

    @Schema(description = "Observações sobre a execução", 
            example = "Terreno mais duro que o previsto, necessário equipamento adicional")
    private String notes;

    @Schema(description = "Data de criação do registro", example = "2024-03-15T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-03-15T16:45:00")
    private LocalDateTime updatedAt;
} 