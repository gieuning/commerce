package com.gieun.commerce.domain.order.entity;

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
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  Order order;

  @Column(nullable = false)
  Long productId;

  Long optionCombinationId;

  @Column(nullable = false)
  String productName;

  @Column(columnDefinition = "TEXT")
  String optionValues;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal unitPrice;

  @Column(nullable = false)
  int quantity;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal subtotal;

  void assignOrder(Order order) {
    this.order = Objects.requireNonNull(order, "주문은 필수입니다.");
  }

  public static OrderItem create(
      Long productId,
      Long optionCombinationId,
      String productName,
      String optionValues,
      BigDecimal unitPrice,
      int quantity
  ) {
    validateQuantity(quantity);
    Objects.requireNonNull(productId, "상품 ID는 필수입니다.");
    Objects.requireNonNull(productName, "상품명은 필수입니다.");
    Objects.requireNonNull(unitPrice, "상품 가격은 필수입니다.");

    return OrderItem.builder()
        .productId(productId)
        .optionCombinationId(optionCombinationId)
        .productName(productName)
        .optionValues(optionValues)
        .unitPrice(unitPrice)
        .quantity(quantity)
        .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
        .build();
  }

  private static void validateQuantity(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
    }
  }
}
