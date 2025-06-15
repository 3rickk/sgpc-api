package br.com.sgpc.sgpc_api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestCreateDto {
    
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projectId;
    
    private LocalDate neededDate;
    
    private String observations;
    
    @NotEmpty(message = "Lista de itens não pode estar vazia")
    @Valid
    private List<MaterialRequestCreateItemDto> items;
} 