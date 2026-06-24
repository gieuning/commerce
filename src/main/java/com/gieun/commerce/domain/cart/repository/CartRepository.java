package com.gieun.commerce.domain.cart.repository;

import com.gieun.commerce.domain.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  @EntityGraph(attributePaths = "items")
  Optional<Cart> findByUserId(Long userId);
}
