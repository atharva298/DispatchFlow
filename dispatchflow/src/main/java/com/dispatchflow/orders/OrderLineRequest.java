package com.dispatchflow.orders;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OrderLineRequest(
		@NotNull(message = "productId is required") UUID productId,
		@Positive(message = "quantity must be greater than zero") int quantity) {
}
