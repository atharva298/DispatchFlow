package com.dispatchflow.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, OrderStatus status, BigDecimal totalAmount,
		Instant createdAt, List<OrderItemResponse> items) {
	static OrderResponse from(CustomerOrder order) {
		return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(),
				order.getCreatedAt(), order.getItems().stream().map(OrderItemResponse::from).toList());
	}
}
