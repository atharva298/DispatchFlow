package com.dispatchflow.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
		@NotEmpty(message = "at least one order item is required")
		@Size(max = 50, message = "an order may contain at most 50 items")
		List<@Valid OrderLineRequest> items) {
}
