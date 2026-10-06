package com.dispatchflow.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations", uniqueConstraints = @UniqueConstraint(
		name = "uk_inventory_reservation_item_stock", columnNames = {"order_item_id", "inventory_id"}))
public class InventoryReservation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "order_item_id", nullable = false)
	private UUID orderItemId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inventory_id", nullable = false)
	private Inventory inventory;

	@Column(nullable = false)
	private int quantity;

	protected InventoryReservation() {
		// Required by JPA.
	}

	public InventoryReservation(UUID orderItemId, Inventory inventory, int quantity) {
		this.orderItemId = orderItemId;
		this.inventory = inventory;
		this.quantity = quantity;
	}

	public UUID getId() { return id; }
	public UUID getOrderItemId() { return orderItemId; }
	public Inventory getInventory() { return inventory; }
	public int getQuantity() { return quantity; }
}
