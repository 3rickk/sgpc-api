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
import br.com.sgpc.sgpc_api.exception.InsufficientStockException;
import br.com.sgpc.sgpc_api.exception.InvalidMovementTypeException;
import br.com.sgpc.sgpc_api.exception.MaterialAlreadyExistsException;
import br.com.sgpc.sgpc_api.exception.MaterialNotFoundException;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;

/**
 * Serviço responsável pelo gerenciamento de materiais de construção.
 * 
 * Esta classe implementa todas as operações relacionadas aos materiais,
 * incluindo CRUD completo, controle de estoque, alertas de baixo estoque
 * e operações de movimentação de entrada e saída.
 * 
 * Principais funcionalidades:
 * - CRUD completo de materiais
 * - Controle de estoque com validações
 * - Alertas de materiais com baixo estoque
 * - Busca por nome e fornecedor
 * - Movimentações de entrada e saída
 * - Soft delete para preservar histórico
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    /**
     * Cria um novo material no sistema.
     * 
     * Valida se não existe outro material com o mesmo nome e cria
     * um novo registro com estoque inicial e configurações padrão.
     * 
     * @param materialCreateDto dados do material a ser criado
     * @return MaterialDto dados do material criado
     * @throws MaterialAlreadyExistsException se já existir material com o mesmo nome
     */
    public MaterialDto createMaterial(MaterialCreateDto materialCreateDto) {
        if (materialRepository.existsByName(materialCreateDto.getName())) {
            throw new MaterialAlreadyExistsException("Já existe um material cadastrado com o nome: " + materialCreateDto.getName());
        }

        Material material = new Material();
        material.setName(materialCreateDto.getName());
        material.setDescription(materialCreateDto.getDescription());
        material.setUnitOfMeasure(materialCreateDto.getUnit());
        material.setUnitPrice(materialCreateDto.getUnitCost());
        material.setSupplier(materialCreateDto.getSupplier());
        material.setCurrentStock(materialCreateDto.getCurrentStock() != null ? 
                                BigDecimal.valueOf(materialCreateDto.getCurrentStock()) : BigDecimal.ZERO);
        material.setMinimumStock(materialCreateDto.getMinimumStock() != null ? 
                               BigDecimal.valueOf(materialCreateDto.getMinimumStock()) : BigDecimal.ZERO);
        material.setIsActive(true);

        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    /**
     * Lista todos os materiais ativos do sistema.
     * 
     * Retorna apenas materiais ativos ordenados por nome para
     * facilitar localização e seleção.
     * 
     * @return List<MaterialDto> lista de materiais ativos
     */
    @Transactional(readOnly = true)
    public List<MaterialDto> getAllMaterials() {
        return materialRepository.findAllActiveMaterialsOrderedByName().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um material pelo ID.
     * 
     * Retorna apenas materiais ativos para evitar acesso a
     * materiais que foram desativados.
     * 
     * @param id ID do material
     * @return Optional<MaterialDto> material encontrado ou empty
     */
    @Transactional(readOnly = true)
    public Optional<MaterialDto> getMaterialById(Long id) {
        return materialRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToDto);
    }

    /**
     * Busca materiais por nome (pesquisa parcial).
     * 
     * Realiza busca case-insensitive no nome do material,
     * retornando apenas materiais ativos.
     * 
     * @param name nome ou parte do nome para busca
     * @return List<MaterialDto> materiais encontrados
     */
    @Transactional(readOnly = true)
    public List<MaterialDto> searchMaterialsByName(String name) {
        return materialRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca materiais por fornecedor.
     * 
     * Realiza busca case-insensitive no nome do fornecedor,
     * útil para análise de produtos por fornecedor.
     * 
     * @param supplier nome ou parte do nome do fornecedor
     * @return List<MaterialDto> materiais do fornecedor
     */
    @Transactional(readOnly = true)
    public List<MaterialDto> getMaterialsBySupplier(String supplier) {
        return materialRepository.findBySupplierContainingIgnoreCaseAndIsActiveTrue(supplier).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista materiais com estoque abaixo do mínimo.
     * 
     * Identifica materiais que precisam de reposição urgente,
     * facilitando o controle de estoque e compras.
     * 
     * @return List<MaterialDto> materiais com baixo estoque
     */
    @Transactional(readOnly = true)
    public List<MaterialDto> getMaterialsBelowMinimumStock() {
        return materialRepository.findMaterialsBelowMinimumStock().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza dados de um material existente.
     * 
     * Permite atualização parcial dos dados, validando se
     * o novo nome não está em uso por outro material.
     * 
     * @param id ID do material a ser atualizado
     * @param materialUpdateDto dados para atualização
     * @return MaterialDto material atualizado
     * @throws MaterialNotFoundException se material não for encontrado
     * @throws MaterialAlreadyExistsException se nome já existir
     */
    public MaterialDto updateMaterial(Long id, MaterialUpdateDto materialUpdateDto) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MaterialNotFoundException("Material com ID " + id + " não foi encontrado"));

        // Validar se o nome já existe (apenas se for diferente do atual)
        if (materialUpdateDto.getName() != null && 
            !material.getName().equals(materialUpdateDto.getName()) &&
            materialRepository.existsByName(materialUpdateDto.getName())) {
            throw new MaterialAlreadyExistsException("Já existe um material cadastrado com o nome: " + materialUpdateDto.getName());
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

    /**
     * Remove um material do sistema (soft delete).
     * 
     * Marca o material como inativo em vez de excluí-lo fisicamente,
     * preservando o histórico de uso em projetos e tarefas.
     * 
     * @param id ID do material a ser removido
     * @throws MaterialNotFoundException se o material não for encontrado
     */
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MaterialNotFoundException("Material com ID " + id + " não foi encontrado"));
        
        // Soft delete - apenas marca como inativo
        material.setIsActive(false);
        materialRepository.save(material);
    }

    /**
     * Atualiza estoque de um material.
     * 
     * Processa movimentações de entrada ou saída de estoque
     * com validações para evitar estoque negativo.
     * 
     * @param id ID do material
     * @param stockMovementDto dados da movimentação (tipo e quantidade)
     * @return MaterialDto material com estoque atualizado
     * @throws MaterialNotFoundException se material não for encontrado
     * @throws InvalidMovementTypeException se tipo inválido
     * @throws InsufficientStockException se estoque insuficiente
     */
    public MaterialDto updateStock(Long id, StockMovementDto stockMovementDto) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MaterialNotFoundException("Material com ID " + id + " não foi encontrado"));

        String movementType = stockMovementDto.getMovementType().toUpperCase();
        BigDecimal quantity = stockMovementDto.getQuantity();

        try {
            switch (movementType) {
                case "ENTRADA":
                    material.addStock(quantity);
                    break;
                case "SAIDA":
                    material.removeStock(quantity);
                    break;
                default:
                    throw new InvalidMovementTypeException("Tipo de movimentação '" + movementType + "' é inválido. Use 'ENTRADA' ou 'SAIDA'");
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Estoque insuficiente")) {
                throw new InsufficientStockException(e.getMessage());
            }
            throw e;
        }

        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    /**
     * Adiciona quantidade ao estoque de um material.
     * 
     * Método conveniente para entrada de estoque, utilizado
     * em recebimentos e compras de materiais.
     * 
     * @param id ID do material
     * @param quantity quantidade a ser adicionada
     * @return MaterialDto material com estoque atualizado
     * @throws MaterialNotFoundException se material não for encontrado
     */
    public MaterialDto addStock(Long id, BigDecimal quantity) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MaterialNotFoundException("Material com ID " + id + " não foi encontrado"));

        // Validar se a quantidade é positiva
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        material.addStock(quantity);
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    /**
     * Remove quantidade do estoque de um material.
     * 
     * Método conveniente para saída de estoque, utilizado
     * em consumo de materiais em projetos e tarefas.
     * 
     * @param id ID do material
     * @param quantity quantidade a ser removida
     * @return MaterialDto material com estoque atualizado
     * @throws MaterialNotFoundException se material não for encontrado
     * @throws InsufficientStockException se estoque insuficiente
     */
    public MaterialDto removeStock(Long id, BigDecimal quantity) {
        Material material = materialRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MaterialNotFoundException("Material com ID " + id + " não foi encontrado"));

        try {
            material.removeStock(quantity);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Estoque insuficiente")) {
                throw new InsufficientStockException(e.getMessage());
            }
            throw e;
        }
        
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    /**
     * Converte entidade Material para MaterialDto.
     * 
     * Inclui cálculo automático do indicador de baixo estoque
     * para facilitar a identificação de materiais críticos.
     * 
     * @param material entidade do material
     * @return MaterialDto dados do material para API
     */
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