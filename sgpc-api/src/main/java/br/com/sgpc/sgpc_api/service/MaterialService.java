package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.MaterialCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialDto;
import br.com.sgpc.sgpc_api.dto.MaterialUpdateDto;
import br.com.sgpc.sgpc_api.dto.StockMovementDto;
import br.com.sgpc.sgpc_api.entity.Material;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;

@Service
@Transactional
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public MaterialDto createMaterial(MaterialCreateDto materialCreateDto) {
        if (materialRepository.existsByName(materialCreateDto.getName())) {
            throw new RuntimeException("Já existe um material com este nome!");
        }

        Material material = new Material();
        material.setName(materialCreateDto.getName());
        material.setDescription(materialCreateDto.getDescription());
        material.setUnitOfMeasure(materialCreateDto.getUnitOfMeasure());
        material.setUnitPrice(materialCreateDto.getUnitPrice());
        material.setSupplier(materialCreateDto.getSupplier());
        material.setCurrentStock(materialCreateDto.getCurrentStock() != null ? 
                                materialCreateDto.getCurrentStock() : BigDecimal.ZERO);
        material.setMinimumStock(materialCreateDto.getMinimumStock() != null ? 
                               materialCreateDto.getMinimumStock() : BigDecimal.ZERO);
        material.setIsActive(true);

        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> getAllMaterials() {
        return materialRepository.findAllActiveMaterialsOrderedByName().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MaterialDto> getMaterialById(Long id) {
        return materialRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> searchMaterialsByName(String name) {
        return materialRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> getMaterialsBySupplier(String supplier) {
        return materialRepository.findBySupplierContainingIgnoreCaseAndIsActiveTrue(supplier).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> getMaterialsBelowMinimumStock() {
        return materialRepository.findMaterialsBelowMinimumStock().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MaterialDto updateMaterial(Long id, MaterialUpdateDto materialUpdateDto) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        // Validar se o nome já existe (apenas se for diferente do atual)
        if (materialUpdateDto.getName() != null && 
            !material.getName().equals(materialUpdateDto.getName()) &&
            materialRepository.existsByName(materialUpdateDto.getName())) {
            throw new RuntimeException("Já existe um material com este nome!");
        }

        // Atualizar campos apenas se fornecidos
        if (materialUpdateDto.getName() != null) {
            material.setName(materialUpdateDto.getName());
        }
        if (materialUpdateDto.getDescription() != null) {
            material.setDescription(materialUpdateDto.getDescription());
        }
        if (materialUpdateDto.getUnitOfMeasure() != null) {
            material.setUnitOfMeasure(materialUpdateDto.getUnitOfMeasure());
        }
        if (materialUpdateDto.getUnitPrice() != null) {
            material.setUnitPrice(materialUpdateDto.getUnitPrice());
        }
        if (materialUpdateDto.getSupplier() != null) {
            material.setSupplier(materialUpdateDto.getSupplier());
        }
        if (materialUpdateDto.getMinimumStock() != null) {
            material.setMinimumStock(materialUpdateDto.getMinimumStock());
        }
        if (materialUpdateDto.getIsActive() != null) {
            material.setIsActive(materialUpdateDto.getIsActive());
        }

        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    public void deleteMaterial(Long id) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));
        
        // Soft delete - apenas marca como inativo
        material.setIsActive(false);
        materialRepository.save(material);
    }

    public MaterialDto updateStock(Long id, StockMovementDto stockMovementDto) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        String movementType = stockMovementDto.getMovementType().toUpperCase();
        BigDecimal quantity = stockMovementDto.getQuantity();

        switch (movementType) {
            case "ENTRADA":
                material.addStock(quantity);
                break;
            case "SAIDA":
                material.removeStock(quantity);
                break;
            default:
                throw new RuntimeException("Tipo de movimentação inválido. Use 'ENTRADA' ou 'SAIDA'");
        }

        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    public MaterialDto addStock(Long id, BigDecimal quantity) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        material.addStock(quantity);
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    public MaterialDto removeStock(Long id, BigDecimal quantity) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        material.removeStock(quantity);
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    private MaterialDto convertToDto(Material material) {
        MaterialDto dto = new MaterialDto();
        dto.setId(material.getId());
        dto.setName(material.getName());
        dto.setDescription(material.getDescription());
        dto.setUnitOfMeasure(material.getUnitOfMeasure());
        dto.setUnitPrice(material.getUnitPrice());
        dto.setSupplier(material.getSupplier());
        dto.setCurrentStock(material.getCurrentStock());
        dto.setMinimumStock(material.getMinimumStock());
        dto.setIsActive(material.getIsActive());
        dto.setIsBelowMinimum(material.isStockBelowMinimum());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        return dto;
    }
} 