package com.dispatchflow.inventory;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
	private final InventoryService inventory;

	public InventoryController(InventoryService inventory) {
		this.inventory = inventory;
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryCreateRequest request) {
		Inventory created = inventory.create(request.productId(), request.warehouseId(), request.availableQuantity());
		return ResponseEntity.created(URI.create("/api/v1/inventory/" + created.getProduct().getId()))
				.body(InventoryResponse.from(created));
	}

	@GetMapping("/{productId}")
	public List<InventoryResponse> findForProduct(@PathVariable UUID productId) {
		return inventory.findForProduct(productId).stream().map(InventoryResponse::from).toList();
	}
}
