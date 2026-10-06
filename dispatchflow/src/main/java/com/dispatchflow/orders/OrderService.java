package com.dispatchflow.orders;

import com.dispatchflow.common.api.InvalidRequestException;
import com.dispatchflow.common.api.ResourceNotFoundException;
import com.dispatchflow.common.api.ConflictException;
import com.dispatchflow.inventory.InventoryService;
import com.dispatchflow.products.Product;
import com.dispatchflow.products.ProductService;
import com.dispatchflow.users.User;
import com.dispatchflow.users.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

		CustomerOrder savedOrder = orders.saveAndFlush(order);
		Map<UUID, UUID> itemIdsByProduct = savedOrder.getItems().stream()
				.collect(Collectors.toMap(item -> item.getProduct().getId(), OrderItem::getId));
		if (inventory.reserve(requestedQuantities, itemIdsByProduct)) {
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

	@Transactional(readOnly = true)
	public OrderPageResponse list(UUID userId, int pageNumber, int pageSize) {
		Pageable pageable = PageRequest.of(Math.max(0, pageNumber), Math.max(1, Math.min(pageSize, 100)),
				Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
		Page<UUID> orderIds = orders.findOrderIdsByUserId(userId, pageable);
		if (orderIds.isEmpty()) {
			return new OrderPageResponse(List.of(), pageable.getPageNumber(), pageable.getPageSize(),
					orderIds.getTotalElements(), orderIds.getTotalPages(), orderIds.isFirst(), orderIds.isLast());
		}
		Map<UUID, CustomerOrder> ordersById = orders.findAllByUser_IdAndIdIn(userId, orderIds.getContent())
				.stream().collect(Collectors.toMap(CustomerOrder::getId, order -> order));
		List<OrderResponse> content = orderIds.getContent().stream()
				.map(ordersById::get).filter(java.util.Objects::nonNull)
				.map(OrderResponse::from).toList();
		return new OrderPageResponse(content, pageable.getPageNumber(), pageable.getPageSize(),
				orderIds.getTotalElements(), orderIds.getTotalPages(), orderIds.isFirst(), orderIds.isLast());
	}

	@Transactional
	public OrderResponse cancel(UUID orderId, UUID userId) {
		CustomerOrder order = orders.findForUpdateByIdAndUserId(orderId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		if (order.getStatus() != OrderStatus.CONFIRMED) {
			throw new ConflictException("Only confirmed orders can be cancelled");
		}
		inventory.releaseForOrderItems(order.getItems().stream().map(OrderItem::getId).toList());
		order.cancel();
		return OrderResponse.from(order);
	}
}
