package com.dispatchflow.shipments;

public enum ShipmentStatus {
	CREATED,
	DISPATCHED,
	IN_TRANSIT,
	OUT_FOR_DELIVERY,
	DELIVERED,
	FAILED;

	public boolean canTransitionTo(ShipmentStatus next) {
		if (next == FAILED) {
			return this != DELIVERED && this != FAILED;
		}
		return switch (this) {
			case CREATED -> next == DISPATCHED;
			case DISPATCHED -> next == IN_TRANSIT;
			case IN_TRANSIT -> next == OUT_FOR_DELIVERY;
			case OUT_FOR_DELIVERY -> next == DELIVERED;
			case DELIVERED, FAILED -> false;
		};
	}
}
