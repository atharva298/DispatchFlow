package com.dispatchflow.inventory;

import com.dispatchflow.common.api.ConflictException;
import com.dispatchflow.products.Product;
import com.dispatchflow.products.ProductService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
	private final InventoryRepository inventory;
	private final ProductService products;

	public InventoryService(InventoryRepository inventory, ProductService products) {
		this.inventory = inventory;
		this.products = products;
	}

	@Transactional
	public Inventory create(UUID productId, String warehouseId, int availableQuantity) {
		Product product = products.get(productId);
		String normalizedWarehouseId = warehouseId.trim().toUpperCase(Locale.ROOT);
		if (inventory.existsByProductIdAndWarehouseId(productId, normalizedWarehouseId)) {
			throw new ConflictException("Inventory already exists for this product and warehouse");
		}
		return inventory.save(new Inventory(product, normalizedWarehouseId, availableQuantity));
	}

	@Transactional(readOnly = true)
	public List<Inventory> findForProduct(UUID productId) {
		products.get(productId);
		return inventory.findAllByProductIdOrderByWarehouseId(productId);
	}
}
