package com.dispatchflow.inventory;

import com.dispatchflow.products.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(
		name = "uk_inventory_product_warehouse", columnNames = {"product_id", "warehouse_id"}))
public class Inventory {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "warehouse_id", nullable = false, length = 64)
	private String warehouseId;

	@Column(name = "available_quantity", nullable = false)
	private int availableQuantity;

	@Column(name = "reserved_quantity", nullable = false)
	private int reservedQuantity;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Inventory() {
		// Required by JPA.
	}

	public Inventory(Product product, String warehouseId, int availableQuantity) {
		this.product = product;
		this.warehouseId = warehouseId;
		this.availableQuantity = availableQuantity;
		this.reservedQuantity = 0;
	}

	@PrePersist
	@PreUpdate
	void updateTimestamp() {
		updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public Product getProduct() { return product; }
	public String getWarehouseId() { return warehouseId; }
	public int getAvailableQuantity() { return availableQuantity; }
	public int getReservedQuantity() { return reservedQuantity; }
	public Instant getUpdatedAt() { return updatedAt; }

	void reserve(int quantity) {
		if (quantity <= 0 || quantity > availableQuantity) {
			throw new IllegalArgumentException("Reservation must be positive and within available stock");
		}
		availableQuantity -= quantity;
		reservedQuantity += quantity;
	}
}
