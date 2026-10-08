package com.dispatchflow.shipments;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

	boolean existsByOrder_Id(UUID orderId);

	Optional<Shipment> findByTrackingNumber(String trackingNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Shipment s where s.trackingNumber = :trackingNumber")
	Optional<Shipment> findForUpdateByTrackingNumber(@Param("trackingNumber") String trackingNumber);
}
