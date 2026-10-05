package com.dispatchflow.orders;

import com.dispatchflow.common.api.InvalidRequestException;
import com.dispatchflow.common.api.ResourceNotFoundException;
import com.dispatchflow.inventory.InventoryService;
import com.dispatchflow.products.Product;
import com.dispatchflow.products.ProductService;
import com.dispatchflow.users.User;
import com.dispatchflow.users.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orders;
	private final UserRepository users;
	private final ProductService products;
	private final InventoryService inventory;

	public OrderService(OrderRepository orders, UserRepository users,
			ProductService products, InventoryService inventory) {
		this.orders = orders;
		this.users = users;
		this.products = products;
		this.inventory = inventory;
	}

	@Transactional
	public OrderResponse create(UUID userId, CreateOrderRequest request) {
		User user = users.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Map<UUID, Product> orderProducts = new LinkedHashMap<>();
		Map<UUID, Integer> requestedQuantities = new LinkedHashMap<>();
		CustomerOrder order = new CustomerOrder(user);

		for (OrderLineRequest line : request.items()) {
			if (orderProducts.containsKey(line.productId())) {
				throw new InvalidRequestException("Each product may appear only once in an order");
			}
			Product product = products.get(line.productId());
			if (!product.isActive()) {
				throw new InvalidRequestException("Inactive products cannot be ordered");
			}
			orderProducts.put(product.getId(), product);
			requestedQuantities.put(product.getId(), line.quantity());
			order.addItem(product, line.quantity());
		}

		if (inventory.reserve(requestedQuantities)) {
			order.confirm();
		} else {
			order.cancel();
		}
		return OrderResponse.from(orders.save(order));
	}

	@Transactional(readOnly = true)
	public OrderResponse get(UUID orderId, UUID userId) {
		CustomerOrder order = orders.findByIdAndUser_Id(orderId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		return OrderResponse.from(order);
	}
}
