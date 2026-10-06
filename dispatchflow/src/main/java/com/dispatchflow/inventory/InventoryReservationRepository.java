package com.dispatchflow.inventory;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from InventoryReservation r join fetch r.inventory "
			+ "where r.orderItemId in :orderItemIds order by r.inventory.id")
	List<InventoryReservation> lockAllForOrderItems(@Param("orderItemIds") Collection<UUID> orderItemIds);

	void deleteAllByOrderItemIdIn(Collection<UUID> orderItemIds);
}
