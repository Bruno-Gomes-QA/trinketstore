package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateOrderItemRequest;
import com.reducess.trinketstore.dto.OrderItemResponse;
import com.reducess.trinketstore.dto.UpdateOrderItemRequest;
import com.reducess.trinketstore.entity.OrderItem;
import com.reducess.trinketstore.exception.OrderItemNotFoundException;
import com.reducess.trinketstore.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Transactional
    public OrderItemResponse createOrderItem(CreateOrderItemRequest request, Integer orderId) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(request.getProductId());
        orderItem.setQtyItems(request.getQtyItems());
        orderItem.setUnitAmount(request.getUnitAmount());
        orderItem.setSubtotalAmount(request.getSubtotalAmount());

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return mapToOrderItemResponse(savedOrderItem);
    }

    @Transactional(readOnly = true)
    public OrderItemResponse getOrderItemById(Integer id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderItemNotFoundException(id));
        return mapToOrderItemResponse(orderItem);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getAllOrderItems() {
        return orderItemRepository.findAll().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderItemResponse updateOrderItem(Integer id, UpdateOrderItemRequest request) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderItemNotFoundException(id));

        if (request.getOrderId() != null) {
            orderItem.setOrderId(request.getOrderId());
        }

        if (request.getProductId() != null) {
            orderItem.setProductId(request.getProductId());
        }

        if (request.getQtyItems() != null) {
            orderItem.setQtyItems(request.getQtyItems());
        }

        if (request.getUnitAmount() != null) {
            orderItem.setUnitAmount(request.getUnitAmount());
        }

        if (request.getSubtotalAmount() != null) {
            orderItem.setSubtotalAmount(request.getSubtotalAmount());
        }

        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return mapToOrderItemResponse(updatedOrderItem);
    }

    @Transactional
    public void deleteOrderItem(Integer id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderItemNotFoundException(id));
        orderItemRepository.delete(orderItem);
    }

    @Transactional
    public void deleteOrderItemsByOrderId(Integer orderId) {
        orderItemRepository.deleteByOrderId(orderId);
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getIdItems(),
                orderItem.getOrderId(),
                orderItem.getProductId(),
                orderItem.getQtyItems(),
                orderItem.getUnitAmount(),
                orderItem.getSubtotalAmount()
        );
    }
}

