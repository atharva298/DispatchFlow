package com.dispatchflow.shipments;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {
	private final ShipmentService shipments;

	public ShipmentController(ShipmentService shipments) {
		this.shipments = shipments;
	}

	@PostMapping
	public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentCreateRequest request) {
		ShipmentResponse created = shipments.create(request.orderId(), request.carrier(), request.estimatedDeliveryDate());
		return ResponseEntity.created(URI.create("/api/v1/shipments/" + created.trackingNumber())).body(created);
	}

	@GetMapping("/{trackingNumber}")
	public ShipmentResponse getByTrackingNumber(@PathVariable String trackingNumber) {
		return shipments.getByTrackingNumber(trackingNumber);
	}
}
