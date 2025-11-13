package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.CreateInventoryRequest;
import com.reducess.trinketstore.dto.InventoryResponse;
import com.reducess.trinketstore.dto.UpdateInventoryRequest;
import com.reducess.trinketstore.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Endpoints de gerenciamento de inventário")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar inventário (Admin)", description = "Cria um novo registro de inventário")
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar todos os inventários (Admin)", description = "Retorna a lista de todos os inventários")
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        List<InventoryResponse> inventories = inventoryService.getAllInventories();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/in-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar inventários com estoque (Admin)", description = "Retorna inventários com quantidade maior que zero")
    public ResponseEntity<List<InventoryResponse>> getInventoriesWithStock() {
        List<InventoryResponse> inventories = inventoryService.getInventoriesWithStock();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/low-stock/{threshold}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar inventários com estoque baixo (Admin)", description = "Retorna inventários com quantidade abaixo do limiar")
    public ResponseEntity<List<InventoryResponse>> getInventoriesWithLowStock(@PathVariable Integer threshold) {
        List<InventoryResponse> inventories = inventoryService.getInventoriesWithLowStock(threshold);
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obter inventário por ID (Admin)", description = "Retorna os dados de um inventário específico")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Integer id) {
        InventoryResponse response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Obter inventário por ID do produto", description = "Retorna o inventário de um produto específico")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Integer productId) {
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar inventário (Admin)", description = "Atualiza os dados de um inventário")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        InventoryResponse response = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/add-stock/{quantity}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Adicionar estoque (Admin)", description = "Adiciona quantidade ao inventário")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Integer id,
            @PathVariable Integer quantity) {
        InventoryResponse response = inventoryService.addStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/remove-stock/{quantity}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Remover estoque (Admin)", description = "Remove quantidade do inventário")
    public ResponseEntity<InventoryResponse> removeStock(
            @PathVariable Integer id,
            @PathVariable Integer quantity) {
        InventoryResponse response = inventoryService.removeStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar inventário (Admin)", description = "Remove um inventário do sistema")
    public ResponseEntity<Void> deleteInventory(@PathVariable Integer id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}

