package com.dispatchflow.shipments;

import com.dispatchflow.common.api.ConflictException;
import com.dispatchflow.common.api.ResourceNotFoundException;
import com.dispatchflow.orders.CustomerOrder;
import com.dispatchflow.orders.OrderRepository;
import com.dispatchflow.orders.OrderStatus;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {
	private final ShipmentRepository shipments;
	private final OrderRepository orders;

	public ShipmentService(ShipmentRepository shipments, OrderRepository orders) {
		this.shipments = shipments;
		this.orders = orders;
	}

	@Transactional
	public ShipmentResponse create(UUID orderId, String carrier, LocalDate estimatedDeliveryDate) {
		CustomerOrder order = orders.findForUpdateById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		if (order.getStatus() != OrderStatus.CONFIRMED) {
			throw new ConflictException("A shipment can only be created for a confirmed order");
		}
		if (shipments.existsByOrder_Id(orderId)) {
			throw new ConflictException("A shipment already exists for this order");
		}
		Shipment shipment = shipments.save(new Shipment(order, carrier, estimatedDeliveryDate));
		order.beginProcessing();
		return ShipmentResponse.from(shipment);
	}

	@Transactional(readOnly = true)
	public ShipmentResponse getByTrackingNumber(String trackingNumber) {
		String normalizedTrackingNumber = trackingNumber.trim().toUpperCase(Locale.ROOT);
		Shipment shipment = shipments.findByTrackingNumber(normalizedTrackingNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
		return ShipmentResponse.from(shipment);
	}
}
