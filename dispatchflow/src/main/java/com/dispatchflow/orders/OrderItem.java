package com.dispatchflow.orders;

import com.dispatchflow.products.Product;
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
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items", uniqueConstraints = @UniqueConstraint(
		name = "uk_order_items_order_product", columnNames = {"order_id", "product_id"}))
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private CustomerOrder order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	protected OrderItem() {
		// Required by JPA.
	}

	OrderItem(CustomerOrder order, Product product, int quantity) {
		this.order = order;
		this.product = product;
		this.quantity = quantity;
		this.unitPrice = product.getPrice();
		this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public UUID getId() { return id; }
	public Product getProduct() { return product; }
	public int getQuantity() { return quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public BigDecimal getSubtotal() { return subtotal; }
}
