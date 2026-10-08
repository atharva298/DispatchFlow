package com.dispatchflow.shipments;

import com.dispatchflow.orders.CustomerOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private CustomerOrder order;

	@Column(name = "tracking_number", nullable = false, unique = true, length = 40)
	private String trackingNumber;

	@Column(nullable = false, length = 100)
	private String carrier;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ShipmentStatus status;

	@Column(name = "estimated_delivery_date", nullable = false)
	private LocalDate estimatedDeliveryDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Shipment() {
		// Required by JPA.
	}

	public Shipment(CustomerOrder order, String carrier, LocalDate estimatedDeliveryDate) {
		this.order = order;
		this.carrier = carrier.trim();
		this.estimatedDeliveryDate = estimatedDeliveryDate;
		this.trackingNumber = "DF-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
		this.status = ShipmentStatus.CREATED;
	}

	@PrePersist
	void setCreationTimestamps() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void setUpdatedTimestamp() { updatedAt = Instant.now(); }

	public UUID getId() { return id; }
	public CustomerOrder getOrder() { return order; }
	public String getTrackingNumber() { return trackingNumber; }
	public String getCarrier() { return carrier; }
	public ShipmentStatus getStatus() { return status; }
	public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	void transitionTo(ShipmentStatus nextStatus) {
		this.status = nextStatus;
	}
}
