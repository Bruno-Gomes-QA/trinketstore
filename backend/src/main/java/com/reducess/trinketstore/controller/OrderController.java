package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.CreateOrderRequest;
import com.reducess.trinketstore.dto.OrderResponse;
import com.reducess.trinketstore.dto.UpdateOrderRequest;
import com.reducess.trinketstore.service.OrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints de gerenciamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido no sistema")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar todos os pedidos (Admin)", description = "Retorna a lista de todos os pedidos")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obter pedido por ID", description = "Retorna os dados de um pedido específico")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar pedidos por usuário", description = "Retorna os pedidos de um usuário específico")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Integer userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar pedidos por status (Admin)", description = "Retorna os pedidos com um status específico")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/checkout/{checkoutId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obter pedido por checkout ID", description = "Retorna um pedido através do checkout ID")
    public ResponseEntity<OrderResponse> getOrderByCheckoutId(@PathVariable String checkoutId) {
        OrderResponse response = orderService.getOrderByCheckoutId(checkoutId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/{paymentIntent}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obter pedido por payment intent", description = "Retorna um pedido através do payment intent")
    public ResponseEntity<OrderResponse> getOrderByPaymentIntent(@PathVariable String paymentIntent) {
        OrderResponse response = orderService.getOrderByPaymentIntent(paymentIntent);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar pedido", description = "Atualiza os dados de um pedido")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateOrderRequest request) {
        OrderResponse response = orderService.updateOrder(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar pedido (Admin)", description = "Remove um pedido do sistema")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar status do pedido (Admin)", description = "Atualiza apenas o status de um pedido")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatusOrder(status);
        OrderResponse response = orderService.updateOrder(id, request);
        return ResponseEntity.ok(response);
    }
}

