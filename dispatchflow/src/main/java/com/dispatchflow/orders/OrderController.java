package com.dispatchflow.orders;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
	private final OrderService orders;

	public OrderController(OrderService orders) {
		this.orders = orders;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		OrderResponse created = orders.create(UUID.fromString(jwt.getSubject()), request);
		return ResponseEntity.created(URI.create("/api/v1/orders/" + created.id())).body(created);
	}

	@GetMapping("/{id}")
	public OrderResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return orders.get(id, UUID.fromString(jwt.getSubject()));
	}

	@GetMapping
	public OrderPageResponse list(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return orders.list(UUID.fromString(jwt.getSubject()), page, size);
	}

	@PostMapping("/{id}/cancel")
	public OrderResponse cancel(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return orders.cancel(id, UUID.fromString(jwt.getSubject()));
	}
}
