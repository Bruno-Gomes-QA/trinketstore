package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateOrderItemRequest;
import com.reducess.trinketstore.dto.CreateOrderRequest;
import com.reducess.trinketstore.dto.OrderItemResponse;
import com.reducess.trinketstore.dto.OrderResponse;
import com.reducess.trinketstore.dto.UpdateOrderRequest;
import com.reducess.trinketstore.entity.Order;
import com.reducess.trinketstore.entity.OrderItem;
import com.reducess.trinketstore.repository.OrderItemRepository;
import com.reducess.trinketstore.repository.OrderRepository;
import com.reducess.trinketstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Verifica se já existe um pedido com o mesmo checkout_id
        if (orderRepository.existsByCheckoutId(request.getCheckoutId())) {
            throw new RuntimeException("Já existe um pedido com este checkout ID");
        }

        // Verifica se já existe um pedido com o mesmo payment_intent
        if (orderRepository.existsByPaymentIntent(request.getPaymentIntent())) {
            throw new RuntimeException("Já existe um pedido com este payment intent");
        }

        inventoryService.verifyStockAvailability(request.getItems());
        request.getItems().forEach(item -> inventoryService.removeStockByProductId(item.getProductId(), item.getQtyItems()));

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatusOrder(request.getStatusOrder() != null ? request.getStatusOrder() : "pending");
        order.setTotalOrders(request.getTotalOrders());
        order.setCurrencyOrder(request.getCurrencyOrder() != null ? request.getCurrencyOrder() : "BRL");
        order.setCheckoutId(request.getCheckoutId());
        order.setPaymentIntent(request.getPaymentIntent());
        order.setPickupQrToken(request.getPickupQrToken());
        order.setPixQrCodeBase64(request.getPixQrCodeBase64());
        order.setPixExpiresAt(request.getPixExpiresAt());

        Order savedOrder = orderRepository.save(order);

        // Salva os itens do pedido
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateOrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(savedOrder.getIdOrder());
                item.setProductId(itemRequest.getProductId());
                item.setQtyItems(itemRequest.getQtyItems());
                item.setUnitAmount(itemRequest.getUnitAmount());
                item.setSubtotalAmount(itemRequest.getSubtotalAmount());
                orderItemRepository.save(item);
            }
        }

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrder(status).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCheckoutId(String checkoutId) {
        Order order = orderRepository.findByCheckoutId(checkoutId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByPaymentIntent(String paymentIntent) {
        Order order = orderRepository.findByPaymentIntent(paymentIntent)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(Integer id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (request.getStatusOrder() != null && !request.getStatusOrder().isBlank()) {
            order.setStatusOrder(request.getStatusOrder());
        }

        if (request.getTotalOrders() != null) {
            order.setTotalOrders(request.getTotalOrders());
        }

        if (request.getCurrencyOrder() != null && !request.getCurrencyOrder().isBlank()) {
            order.setCurrencyOrder(request.getCurrencyOrder());
        }

        if (request.getCheckoutId() != null && !request.getCheckoutId().isBlank()) {
            if (!request.getCheckoutId().equals(order.getCheckoutId())) {
                if (orderRepository.existsByCheckoutId(request.getCheckoutId())) {
                    throw new RuntimeException("Já existe um pedido com este checkout ID");
                }
                order.setCheckoutId(request.getCheckoutId());
            }
        }

        if (request.getPaymentIntent() != null && !request.getPaymentIntent().isBlank()) {
            if (!request.getPaymentIntent().equals(order.getPaymentIntent())) {
                if (orderRepository.existsByPaymentIntent(request.getPaymentIntent())) {
                    throw new RuntimeException("Já existe um pedido com este payment intent");
                }
                order.setPaymentIntent(request.getPaymentIntent());
            }
        }

        if (request.getPickupQrToken() != null) {
            order.setPickupQrToken(request.getPickupQrToken());
        }

        if (request.getPixQrCodeBase64() != null) {
            order.setPixQrCodeBase64(request.getPixQrCodeBase64());
        }

        if (request.getPixExpiresAt() != null) {
            order.setPixExpiresAt(request.getPixExpiresAt());
        }

        // Atualiza os itens do pedido se fornecidos
        if (request.getItems() != null) {
            // Remove os itens antigos
            orderItemRepository.deleteByOrderId(id);

            // Adiciona os novos itens
            for (CreateOrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(id);
                item.setProductId(itemRequest.getProductId());
                item.setQtyItems(itemRequest.getQtyItems());
                item.setUnitAmount(itemRequest.getUnitAmount());
                item.setSubtotalAmount(itemRequest.getSubtotalAmount());
                orderItemRepository.save(item);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        restoreStock(order);
        // Remove os itens do pedido primeiro
        orderItemRepository.deleteByOrderId(id);

        // Remove o pedido
        orderRepository.delete(order);
    }

    @Transactional
    public void cancelOrderByCustomer(Integer orderId, UserPrincipal currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (!order.getUserId().equals(currentUser.getUserId().intValue())) {
            throw new RuntimeException("Você não pode cancelar este pedido.");
        }
        if (!"pending".equalsIgnoreCase(order.getStatusOrder())) {
            throw new RuntimeException("Apenas pedidos pendentes podem ser cancelados.");
        }

        restoreStock(order);
        orderItemRepository.deleteByOrderId(orderId);
        orderRepository.delete(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setIdOrder(order.getIdOrder());
        response.setUserId(order.getUserId());
        response.setStatusOrder(order.getStatusOrder());
        response.setTotalOrders(order.getTotalOrders());
        response.setCurrencyOrder(order.getCurrencyOrder());
        response.setCheckoutId(order.getCheckoutId());
        response.setPaymentIntent(order.getPaymentIntent());
        response.setPickupQrToken(order.getPickupQrToken());
        response.setPixQrCodeBase64(order.getPixQrCodeBase64());
        response.setPixExpiresAt(order.getPixExpiresAt());
        response.setCreatedAt(order.getCreatedAt());

        // Busca os itens do pedido
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getIdOrder());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setIdItems(item.getIdItems());
        response.setOrderId(item.getOrderId());
        response.setProductId(item.getProductId());
        response.setQtyItems(item.getQtyItems());
        response.setUnitAmount(item.getUnitAmount());
        response.setSubtotalAmount(item.getSubtotalAmount());
        return response;
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getIdOrder());
        for (OrderItem item : items) {
            inventoryService.addStockByProductId(item.getProductId(), item.getQtyItems());
        }
    }
}
