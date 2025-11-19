package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateInventoryRequest;
import com.reducess.trinketstore.dto.InventoryResponse;
import com.reducess.trinketstore.dto.UpdateInventoryRequest;
import com.reducess.trinketstore.entity.Inventory;
import com.reducess.trinketstore.exception.InventoryConflictException;
import com.reducess.trinketstore.exception.InventoryNotFoundException;
import com.reducess.trinketstore.repository.InventoryRepository;
import com.reducess.trinketstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        if (request.getQtyOnHand() == null || request.getQtyOnHand() < 0) {
            throw new InventoryConflictException("Quantidade inicial inválida");
        }
        // Verifica se o produto existe
        if (!productRepository.existsById(request.getProductId())) {
            throw new InventoryNotFoundException("Produto não encontrado");
        }

        // Verifica se já existe inventário para este produto
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new InventoryConflictException("Já existe inventário para este produto");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(request.getProductId());
        inventory.setQtyOnHand(request.getQtyOnHand());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToInventoryResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse ensureInventoryRecord(Integer productId, Integer qtyOnHand) {
        return inventoryRepository.findByProductId(productId)
                .map(this::mapToInventoryResponse)
                .orElseGet(() -> {
                    int sanitizedQty = qtyOnHand != null && qtyOnHand >= 0 ? qtyOnHand : 0;
                    CreateInventoryRequest request = new CreateInventoryRequest(productId, sanitizedQty);
                    return createInventory(request);
                });
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Integer id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado"));
        return mapToInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado para este produto"));
        return mapToInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToInventoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesWithStock() {
        return inventoryRepository.findByQtyOnHandGreaterThan(0).stream()
                .map(this::mapToInventoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesWithLowStock(Integer threshold) {
        return inventoryRepository.findByQtyOnHandLessThan(threshold).stream()
                .map(this::mapToInventoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponse updateInventory(Integer id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado"));

        // Se está alterando o produto, verifica se o novo produto existe
        if (request.getProductId() != null && !request.getProductId().equals(inventory.getProductId())) {
            if (!productRepository.existsById(request.getProductId())) {
                throw new InventoryNotFoundException("Produto não encontrado");
            }
            // Verifica se já existe inventário para o novo produto
            if (inventoryRepository.existsByProductId(request.getProductId())) {
                throw new InventoryConflictException("Já existe inventário para este produto");
            }
            inventory.setProductId(request.getProductId());
        }

        if (request.getQtyOnHand() != null) {
            inventory.setQtyOnHand(request.getQtyOnHand());
        }

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToInventoryResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse addStock(Integer id, Integer quantity) {
        if (quantity <= 0) {
            throw new InventoryConflictException("Quantidade deve ser maior que zero");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado"));

        inventory.setQtyOnHand(inventory.getQtyOnHand() + quantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToInventoryResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse removeStock(Integer id, Integer quantity) {
        if (quantity <= 0) {
            throw new InventoryConflictException("Quantidade deve ser maior que zero");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado"));

        int newQuantity = inventory.getQtyOnHand() - quantity;
        if (newQuantity < 0) {
            throw new InventoryConflictException("Quantidade insuficiente em estoque");
        }

        inventory.setQtyOnHand(newQuantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToInventoryResponse(updatedInventory);
    }

    @Transactional
    public void deleteInventory(Integer id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventário não encontrado"));
        inventoryRepository.delete(inventory);
    }

    @Transactional
    public void deleteInventoryByProductId(Integer productId) {
        inventoryRepository.findByProductId(productId)
                .ifPresent(inventoryRepository::delete);
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setIdInventory(inventory.getIdInventory());
        response.setProductId(inventory.getProductId());
        response.setQtyOnHand(inventory.getQtyOnHand());

        productRepository.findById(inventory.getProductId()).ifPresent(product -> {
            response.setNomeProduct(product.getNomeProduct());
            response.setSlugProduct(product.getSlugProduct());
            response.setCategoriaProduct(product.getCategoriaProduct());
            response.setAtivoProduct(product.getAtivo());
            response.setImagemurlProduct(product.getImagemurlProduct());
        });

        return response;
    }
}
