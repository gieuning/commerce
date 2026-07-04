package com.gieun.commerce.domain.cart.repository;

import com.gieun.commerce.domain.cart.entity.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

  @EntityGraph(attributePaths = "items")
  Optional<Cart> findByUserId(Long userId);

  @EntityGraph(attributePaths = "items")
  Optional<Cart> findByGuestToken(String guestToken);

  // 방치 게스트 카트 정리: 엔티티/아이템을 로딩하지 않고 ID만 배치로 뽑아 벌크 삭제한다.
  // (@EntityGraph로 컬렉션 fetch join + Pageable을 쓰면 메모리 페이징이 되어 배치 limit이 무력화되므로 ID 프로젝션 사용)
  @Query("select c.id from Cart c where c.guestToken is not null and c.createdAt < :cutoff")
  List<Long> findAbandonedGuestCartIds(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

  @Modifying
  @Query("delete from CartItem ci where ci.cart.id in :cartIds")
  void deleteItemsByCartIds(@Param("cartIds") List<Long> cartIds);

  @Modifying
  @Query("delete from Cart c where c.id in :cartIds")
  void deleteByIds(@Param("cartIds") List<Long> cartIds);
}
