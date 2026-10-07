package com.dispatchflow.orders;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

	@EntityGraph(attributePaths = {"items", "items.product"})
	Optional<CustomerOrder> findByIdAndUser_Id(UUID id, UUID userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = {"items", "items.product"})
	@Query("select o from CustomerOrder o where o.id = :id and o.user.id = :userId")
	Optional<CustomerOrder> findForUpdateByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from CustomerOrder o where o.id = :id")
	Optional<CustomerOrder> findForUpdateById(@Param("id") UUID id);

	@Query("select o.id from CustomerOrder o where o.user.id = :userId")
	Page<UUID> findOrderIdsByUserId(@Param("userId") UUID userId, Pageable pageable);

	@EntityGraph(attributePaths = {"items", "items.product"})
	List<CustomerOrder> findAllByUser_IdAndIdIn(UUID userId, Collection<UUID> ids);
}
