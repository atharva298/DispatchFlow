package com.dispatchflow.orders;

import com.dispatchflow.users.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OrderStatus status;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected CustomerOrder() {
		// Required by JPA.
	}

	CustomerOrder(User user) {
		this.user = user;
		this.status = OrderStatus.PENDING;
	}

	void addItem(com.dispatchflow.products.Product product, int quantity) {
		OrderItem item = new OrderItem(this, product, quantity);
		items.add(item);
		totalAmount = totalAmount.add(item.getSubtotal());
	}

	void confirm() { status = OrderStatus.CONFIRMED; }
	void cancel() { status = OrderStatus.CANCELLED; }

	@PrePersist
	void setCreationTimestamps() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void setUpdatedTimestamp() { updatedAt = Instant.now(); }

	public UUID getId() { return id; }
	public User getUser() { return user; }
	public OrderStatus getStatus() { return status; }
	public BigDecimal getTotalAmount() { return totalAmount; }
	public List<OrderItem> getItems() { return List.copyOf(items); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
