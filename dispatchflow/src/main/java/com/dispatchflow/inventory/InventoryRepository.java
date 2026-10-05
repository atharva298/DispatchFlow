package com.dispatchflow.inventory;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

	boolean existsByProductIdAndWarehouseId(UUID productId, String warehouseId);

	List<Inventory> findAllByProductIdOrderByWarehouseId(UUID productId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Inventory i where i.product.id in :productIds order by i.product.id, i.warehouseId")
	List<Inventory> lockAllForProducts(@Param("productIds") List<UUID> productIds);
}
