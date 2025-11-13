package com.reducess.trinketstore.controller;
import com.reducess.trinketstore.dto.CreatePriceRequest;
import com.reducess.trinketstore.dto.PriceResponse;
import com.reducess.trinketstore.dto.UpdatePriceRequest;
import com.reducess.trinketstore.service.PriceService;
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
@RequestMapping("/prices")
@RequiredArgsConstructor
@Tag(name = "Prices", description = "Endpoints de gerenciamento de preços")
public class PriceController {
    private final PriceService priceService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar preço (Admin)", description = "Cria um novo preço para um produto")
    public ResponseEntity<PriceResponse> createPrice(@Valid @RequestBody CreatePriceRequest request) {
        PriceResponse response = priceService.createPrice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping
    @Operation(summary = "Listar todos os preços", description = "Retorna a lista de todos os preços")
    public ResponseEntity<List<PriceResponse>> getAllPrices() {
        List<PriceResponse> prices = priceService.getAllPrices();
        return ResponseEntity.ok(prices);
    }
    @GetMapping("/active")
    @Operation(summary = "Listar preços vigentes", description = "Retorna a lista de preços vigentes")
    public ResponseEntity<List<PriceResponse>> getActivePrices() {
        List<PriceResponse> prices = priceService.getActivePrices();
        return ResponseEntity.ok(prices);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Obter preço por ID", description = "Retorna os dados de um preço específico")
    public ResponseEntity<PriceResponse> getPriceById(@PathVariable Integer id) {
        PriceResponse response = priceService.getPriceById(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar preços por produto", description = "Retorna todos os preços de um produto específico")
    public ResponseEntity<List<PriceResponse>> getPricesByProductId(@PathVariable Integer productId) {
        List<PriceResponse> prices = priceService.getPricesByProductId(productId);
        return ResponseEntity.ok(prices);
    }
    @GetMapping("/product/{productId}/current")
    @Operation(summary = "Obter preço vigente do produto", description = "Retorna o preço vigente atual de um produto")
    public ResponseEntity<PriceResponse> getCurrentPriceByProductId(@PathVariable Integer productId) {
        PriceResponse response = priceService.getCurrentPriceByProductId(productId);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar preço (Admin)", description = "Atualiza os dados de um preço")
    public ResponseEntity<PriceResponse> updatePrice(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePriceRequest request) {
        PriceResponse response = priceService.updatePrice(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar preço (Admin)", description = "Remove um preço do sistema")
    public ResponseEntity<Void> deletePrice(@PathVariable Integer id) {
        priceService.deletePrice(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Desativar preço (Admin)", description = "Desativa um preço sem removê-lo do sistema")
    public ResponseEntity<PriceResponse> deactivatePrice(@PathVariable Integer id) {
        PriceResponse response = priceService.deactivatePrice(id);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Ativar preço (Admin)", description = "Ativa um preço desativado")
    public ResponseEntity<PriceResponse> activatePrice(@PathVariable Integer id) {
        PriceResponse response = priceService.activatePrice(id);
        return ResponseEntity.ok(response);
    }
}
