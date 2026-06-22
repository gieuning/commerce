package com.gieun.commerce.domain.cart.entity;

import com.gieun.commerce.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  Cart cart;

  @Column(nullable = false)
  Long productId;

  Long optionCombinationId;

  @Column(nullable = false)
  int quantity;

  public static CartItem create(Long productId, Long optionCombinationId, int quantity) {
    Objects.requireNonNull(productId, "상품 ID는 필수입니다.");
    validateQuantity(quantity);
    return CartItem.builder()
        .productId(productId)
        .optionCombinationId(optionCombinationId)
        .quantity(quantity)
        .build();
  }

  void assignCart(Cart cart) {
    this.cart = Objects.requireNonNull(cart, "카트는 필수입니다.");
  }

  void increaseQuantity(int amount) {
    validateQuantity(amount);
    this.quantity += amount;
  }

  void changeQuantity(int quantity) {
    validateQuantity(quantity);
    this.quantity = quantity;
  }

  boolean matches(Long productId, Long optionCombinationId) {
    return this.productId.equals(productId)
        && Objects.equals(this.optionCombinationId, optionCombinationId);
  }

  private static void validateQuantity(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
    }
  }
}
