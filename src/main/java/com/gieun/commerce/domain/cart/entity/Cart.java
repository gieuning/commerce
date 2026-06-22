package com.gieun.commerce.domain.cart.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cart extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  Long userId;

  String guestToken;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<CartItem> items = new ArrayList<>();

  public static Cart forUser(Long userId) {
    Objects.requireNonNull(userId, "회원 ID는 필수입니다.");
    return Cart.builder().userId(userId).build();
  }

  public static Cart forGuest(String guestToken) {
    Objects.requireNonNull(guestToken, "게스트 토큰은 필수입니다.");
    return Cart.builder().guestToken(guestToken).build();
  }

  public void addItem(Long productId, Long optionCombinationId, int quantity) {
    findItem(productId, optionCombinationId).ifPresentOrElse(
        item -> item.increaseQuantity(quantity),
        () -> {
          CartItem item = CartItem.create(productId, optionCombinationId, quantity);
          items.add(item);
          item.assignCart(this);
        });
  }

  public void changeQuantity(Long productId, Long optionCombinationId, int quantity) {
    findItem(productId, optionCombinationId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_CART_ITEM))
        .changeQuantity(quantity);
  }

  public void removeItem(Long productId, Long optionCombinationId) {
    items.removeIf(item -> item.matches(productId, optionCombinationId));
  }

  public void clear() {
    items.clear();
  }

  private Optional<CartItem> findItem(Long productId, Long optionCombinationId) {
    return items.stream()
        .filter(item -> item.matches(productId, optionCombinationId))
        .findFirst();
  }
}
