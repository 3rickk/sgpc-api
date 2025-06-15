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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @PostMapping
    public ResponseEntity<MaterialDto> createMaterial(@Valid @RequestBody MaterialCreateDto materialCreateDto) {
        try {
            MaterialDto createdMaterial = materialService.createMaterial(materialCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar material: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MaterialDto>> getAllMaterials() {
        List<MaterialDto> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDto> getMaterialById(@PathVariable Long id) {
        return materialService.getMaterialById(id)
                .map(material -> ResponseEntity.ok(material))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialDto> updateMaterial(@PathVariable Long id, 
                                                     @Valid @RequestBody MaterialUpdateDto materialUpdateDto) {
        try {
            MaterialDto updatedMaterial = materialService.updateMaterial(id, materialUpdateDto);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar material: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar material: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<MaterialDto>> searchMaterials(@RequestParam String name) {
        List<MaterialDto> materials = materialService.searchMaterialsByName(name);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/supplier/{supplier}")
    public ResponseEntity<List<MaterialDto>> getMaterialsBySupplier(@PathVariable String supplier) {
        List<MaterialDto> materials = materialService.getMaterialsBySupplier(supplier);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<MaterialDto>> getMaterialsBelowMinimumStock() {
        List<MaterialDto> materials = materialService.getMaterialsBelowMinimumStock();
        return ResponseEntity.ok(materials);
    }

    // Endpoints para controle de estoque
    @PostMapping("/{id}/stock")
    public ResponseEntity<MaterialDto> updateStock(@PathVariable Long id, 
                                                  @Valid @RequestBody StockMovementDto stockMovementDto) {
        try {
            MaterialDto updatedMaterial = materialService.updateStock(id, stockMovementDto);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar estoque: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/stock/add")
    public ResponseEntity<MaterialDto> addStock(@PathVariable Long id, 
                                               @RequestParam BigDecimal quantity) {
        try {
            MaterialDto updatedMaterial = materialService.addStock(id, quantity);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar estoque: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/stock/remove")
    public ResponseEntity<MaterialDto> removeStock(@PathVariable Long id, 
                                                  @RequestParam BigDecimal quantity) {
        try {
            MaterialDto updatedMaterial = materialService.removeStock(id, quantity);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover estoque: " + e.getMessage());
        }
    }
} 