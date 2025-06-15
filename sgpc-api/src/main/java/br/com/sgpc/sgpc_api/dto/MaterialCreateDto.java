package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCreateDto {

    @NotBlank(message = "Nome do material é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String name;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String description;

    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(min = 1, max = 50, message = "Unidade de medida deve ter entre 1 e 50 caracteres")
    private String unitOfMeasure;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    private BigDecimal unitPrice;

    @Size(max = 255, message = "Nome do fornecedor deve ter no máximo 255 caracteres")
    private String supplier;

    @DecimalMin(value = "0", message = "Estoque atual deve ser maior ou igual a zero")
    private BigDecimal currentStock = BigDecimal.ZERO;

    @DecimalMin(value = "0", message = "Estoque mínimo deve ser maior ou igual a zero")
    private BigDecimal minimumStock = BigDecimal.ZERO;
} 