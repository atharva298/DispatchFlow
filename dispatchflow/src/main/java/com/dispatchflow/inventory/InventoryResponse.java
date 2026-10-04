package com.dispatchflow.inventory;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
		UUID id,
		UUID productId,
		String warehouseId,
		int availableQuantity,
		int reservedQuantity,
		Instant updatedAt) {
	static InventoryResponse from(Inventory inventory) {
		return new InventoryResponse(inventory.getId(), inventory.getProduct().getId(),
				inventory.getWarehouseId(), inventory.getAvailableQuantity(),
				inventory.getReservedQuantity(), inventory.getUpdatedAt());
	}
}
