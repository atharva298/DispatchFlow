package com.dispatchflow.orders;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(UUID productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
	static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(item.getProduct().getId(), item.getQuantity(),
				item.getUnitPrice(), item.getSubtotal());
	}
}
