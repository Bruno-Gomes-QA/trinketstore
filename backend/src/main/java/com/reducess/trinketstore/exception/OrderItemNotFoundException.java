package com.reducess.trinketstore.exception;

public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException(String message) {
        super(message);
    }

    public OrderItemNotFoundException(Integer id) {
        super("Item de pedido não encontrado com ID: " + id);
    }
}

