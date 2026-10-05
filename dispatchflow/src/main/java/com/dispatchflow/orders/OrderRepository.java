package com.dispatchflow.orders;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

	@EntityGraph(attributePaths = {"items", "items.product"})
	Optional<CustomerOrder> findByIdAndUser_Id(UUID id, UUID userId);
}
