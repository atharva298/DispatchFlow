package com.dispatchflow.inventory;

import com.dispatchflow.common.api.ConflictException;
import com.dispatchflow.products.Product;
import com.dispatchflow.products.ProductService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
	private final InventoryRepository inventory;
	private final ProductService products;
	private final InventoryReservationRepository reservations;

	public InventoryService(InventoryRepository inventory, InventoryReservationRepository reservations,
			ProductService products) {
		this.inventory = inventory;
		this.reservations = reservations;
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

	@Transactional
	public void releaseForOrderItems(Collection<UUID> orderItemIds) {
		List<InventoryReservation> allocations = reservations.lockAllForOrderItems(orderItemIds);
		for (InventoryReservation allocation : allocations) {
			allocation.getInventory().release(allocation.getQuantity());
		}
		reservations.deleteAll(allocations);
	}

	@Transactional
	public boolean reserve(Map<UUID, Integer> requestedQuantities, Map<UUID, UUID> orderItemIdsByProduct) {
		List<UUID> productIds = requestedQuantities.keySet().stream().sorted().toList();
		List<Inventory> lockedStock = inventory.lockAllForProducts(productIds);
		Map<UUID, List<Inventory>> stockByProduct = new HashMap<>();
		for (Inventory stock : lockedStock) {
			stockByProduct.computeIfAbsent(stock.getProduct().getId(), ignored -> new ArrayList<>()).add(stock);
		}

		for (Map.Entry<UUID, Integer> requested : requestedQuantities.entrySet()) {
			long available = stockByProduct.getOrDefault(requested.getKey(), List.of()).stream()
					.mapToLong(Inventory::getAvailableQuantity).sum();
			if (available < requested.getValue()) {
				return false;
			}
		}

		List<InventoryReservation> allocations = new ArrayList<>();
		for (Map.Entry<UUID, Integer> requested : requestedQuantities.entrySet()) {
			int remaining = requested.getValue();
			for (Inventory stock : stockByProduct.get(requested.getKey()).stream()
					.sorted(Comparator.comparing(Inventory::getWarehouseId)).toList()) {
				int allocated = Math.min(remaining, stock.getAvailableQuantity());
				if (allocated > 0) {
					stock.reserve(allocated);
					allocations.add(new InventoryReservation(
							orderItemIdsByProduct.get(requested.getKey()), stock, allocated));
					remaining -= allocated;
				}
				if (remaining == 0) break;
			}
		}
		reservations.saveAll(allocations);
		return true;
	}

}
