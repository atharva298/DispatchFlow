package com.dispatchflow.inventory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

	boolean existsByProductIdAndWarehouseId(UUID productId, String warehouseId);

	List<Inventory> findAllByProductIdOrderByWarehouseId(UUID productId);
}
