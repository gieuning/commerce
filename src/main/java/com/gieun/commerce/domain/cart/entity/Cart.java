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

  // 게스트 카트를 회원 카트로 전환한다. (user_id 설정 + guest_token 제거 → chk_carts_owner XOR 유지)
  public void assignToUser(Long userId) {
    this.userId = Objects.requireNonNull(userId, "회원 ID는 필수입니다.");
    this.guestToken = null;
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

  public int calculateQuantityAfterAdd(Long productId, Long optionCombinationId, int quantity) {
    return findItem(productId, optionCombinationId)
        .map(item -> item.getQuantity() + quantity)
        .orElse(quantity);
  }

  public int calculateQuantityAfterOptionChange(CartItem item, Long optionCombinationId) {
    validateContains(item);
    if (Objects.equals(item.getOptionCombinationId(), optionCombinationId)) {
      return item.getQuantity();
    }

    return findItem(item.getProductId(), optionCombinationId)
        .map(target -> target.getQuantity() + item.getQuantity())
        .orElse(item.getQuantity());
  }

  public CartItem getItem(Long cartItemId) {
    return items.stream()
        .filter(item -> Objects.equals(item.getId(), cartItemId))
        .findFirst()
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_CART_ITEM));
  }

  public void changeQuantity(Long cartItemId, int quantity) {
    getItem(cartItemId).changeQuantity(quantity);
  }

  public void changeQuantity(CartItem item, int quantity) {
    validateContains(item);
    item.changeQuantity(quantity);
  }

  public void changeOption(CartItem item, Long optionCombinationId) {
    validateContains(item);
    if (Objects.equals(item.getOptionCombinationId(), optionCombinationId)) {
      return;
    }

    findItem(item.getProductId(), optionCombinationId).ifPresentOrElse(
        target -> {
          target.increaseQuantity(item.getQuantity());
          items.remove(item);
        },
        () -> item.changeOption(optionCombinationId)
    );
  }

  public void removeItem(Long cartItemId) {
    boolean removed = items.removeIf(item -> Objects.equals(item.getId(), cartItemId));

    if (!removed) {
      throw new DomainException(DomainExceptionCode.NOT_FOUND_CART_ITEM);
    }
  }

  public void clear() {
    items.clear();
  }

  private Optional<CartItem> findItem(Long productId, Long optionCombinationId) {
    return items.stream()
        .filter(item -> item.matches(productId, optionCombinationId))
        .findFirst();
  }

  private void validateContains(CartItem item) {
    if (item == null || item.getId() == null || items.stream()
        .noneMatch(existing -> Objects.equals(existing.getId(), item.getId()))) {
      throw new DomainException(DomainExceptionCode.NOT_FOUND_CART_ITEM);
    }
  }
}
