package com.dispatchflow.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record InventoryCreateRequest(
		@NotNull(message = "productId is required") UUID productId,
		@NotBlank(message = "warehouseId is required")
		@Size(max = 64, message = "warehouseId must be at most 64 characters") String warehouseId,
		@Min(value = 0, message = "availableQuantity must not be negative") int availableQuantity) {
}
