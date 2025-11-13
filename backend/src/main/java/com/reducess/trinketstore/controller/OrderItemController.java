package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.CreateOrderItemRequest;
import com.reducess.trinketstore.dto.OrderItemResponse;
import com.reducess.trinketstore.dto.UpdateOrderItemRequest;
import com.reducess.trinketstore.service.OrderItemService;
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
@RequestMapping("/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Items", description = "Endpoints de gerenciamento de itens de pedidos")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar item de pedido (Admin)", description = "Adiciona um novo item a um pedido específico")
    public ResponseEntity<OrderItemResponse> createOrderItem(
            @PathVariable Integer orderId,
            @Valid @RequestBody CreateOrderItemRequest request) {
        OrderItemResponse response = orderItemService.createOrderItem(request, orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar todos os itens de pedidos (Admin)", description = "Retorna a lista de todos os itens de pedidos")
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems() {
        List<OrderItemResponse> orderItems = orderItemService.getAllOrderItems();
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obter item de pedido por ID", description = "Retorna os dados de um item de pedido específico")
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable Integer id) {
        OrderItemResponse response = orderItemService.getOrderItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar itens por pedido", description = "Retorna todos os itens de um pedido específico")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(@PathVariable Integer orderId) {
        List<OrderItemResponse> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar item de pedido (Admin)", description = "Atualiza os dados de um item de pedido")
    public ResponseEntity<OrderItemResponse> updateOrderItem(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateOrderItemRequest request) {
        OrderItemResponse response = orderItemService.updateOrderItem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar item de pedido (Admin)", description = "Remove um item de pedido do sistema")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Integer id) {
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar todos os itens de um pedido (Admin)", description = "Remove todos os itens de um pedido específico")
    public ResponseEntity<Void> deleteOrderItemsByOrderId(@PathVariable Integer orderId) {
        orderItemService.deleteOrderItemsByOrderId(orderId);
        return ResponseEntity.noContent().build();
    }
}

