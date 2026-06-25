package com.gieun.commerce.domain.order.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  OrderStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal totalProductPrice;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal discountAmount;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal shippingFee;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal totalPrice;

  @Column(nullable = false)
  LocalDateTime orderedAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<OrderItem> items = new ArrayList<>();


  public static Order create(Long userId) {
    Objects.requireNonNull(userId, "회원 ID는 필수입니다.");

    // 할인/배송비 정책 없어 기본값 0
    return Order.builder()
        .userId(userId)
        .status(OrderStatus.CREATED)
        .totalProductPrice(BigDecimal.ZERO)
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(BigDecimal.ZERO)
        .orderedAt(LocalDateTime.now())
        .build();
  }

  public void addItem(OrderItem item) {
    Objects.requireNonNull(item, "주문 상품은 필수입니다.");

    items.add(item);
    item.assignOrder(this);
    recalculateTotalPrice();
  }

  public void cancel() {
    if (status != OrderStatus.CREATED) {
      throw new DomainException(DomainExceptionCode.CANNOT_CANCEL_ORDER);
    }
    status = OrderStatus.CANCELLED;
  }

  private void recalculateTotalPrice() {
    this.totalProductPrice = items.stream()
        .map(OrderItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    this.totalPrice = totalProductPrice
        .subtract(discountAmount)
        .add(shippingFee);
  }

}
