package com.dispatchflow.shipments;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentResponse(
		UUID id,
		UUID orderId,
		String trackingNumber,
		String carrier,
		ShipmentStatus status,
		LocalDate estimatedDeliveryDate,
		Instant createdAt) {
	static ShipmentResponse from(Shipment shipment) {
		return new ShipmentResponse(shipment.getId(), shipment.getOrder().getId(), shipment.getTrackingNumber(),
				shipment.getCarrier(), shipment.getStatus(), shipment.getEstimatedDeliveryDate(), shipment.getCreatedAt());
	}
}
