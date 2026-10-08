package com.dispatchflow.shipments;

import jakarta.validation.constraints.NotNull;

public record ShipmentStatusUpdateRequest(
		@NotNull(message = "status is required") ShipmentStatus status) {
}
