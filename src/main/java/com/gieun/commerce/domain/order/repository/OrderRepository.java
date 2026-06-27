package com.gieun.commerce.domain.order.repository;

import com.gieun.commerce.domain.order.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

  @EntityGraph(attributePaths = "items")
  Optional<Order> findByIdAndUserId(Long id, Long userId);


  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.id = :id and o.userId = :userId")
  Optional<Order> findByIdAndUserIdForUpdate(Long id, Long userId);
}
