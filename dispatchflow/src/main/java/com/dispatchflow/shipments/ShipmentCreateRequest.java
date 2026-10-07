package com.dispatchflow.shipments;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentCreateRequest(
		@NotNull(message = "orderId is required") UUID orderId,
		@NotBlank(message = "carrier is required")
		@Size(max = 100, message = "carrier must be at most 100 characters") String carrier,
		@NotNull(message = "estimatedDeliveryDate is required")
		@FutureOrPresent(message = "estimatedDeliveryDate cannot be in the past") LocalDate estimatedDeliveryDate) {
}
