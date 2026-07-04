package com.gieun.commerce.domain.cart.repository;

import com.gieun.commerce.domain.cart.entity.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  @EntityGraph(attributePaths = "items")
  Optional<Cart> findByUserId(Long userId);

  @EntityGraph(attributePaths = "items")
  Optional<Cart> findByGuestToken(String guestToken);

  // 게스트 카트(guest_token != null) 중 생성 후 cutoff 이전인 것을 배치(Pageable)로 조회
  List<Cart> findByGuestTokenIsNotNullAndCreatedAtBefore(LocalDateTime cutoff, Pageable pageable);
}
