package com.gieun.commerce.domain.product.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "product_option_combinations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionCombination extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal additionalPrice;

  @Column(nullable = false)
  int stock;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  ProductStatus status;

  @OneToMany(mappedBy = "combination", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<CombinationValue> values = new ArrayList<>();

  public static OptionCombination create(BigDecimal additionalPrice, int stock) {
    return OptionCombination.builder()
        .additionalPrice(additionalPrice == null ? BigDecimal.ZERO : additionalPrice)
        .stock(stock)
        .status(ProductStatus.FOR_SALE)
        .build();
  }

  public void addValue(CombinationValue value) {
    values.add(value);
    value.assignCombination(this);
  }

  public void update(BigDecimal additionalPrice, int stock, ProductStatus status) {
    if (stock < 0) {
      throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
    }
    if (additionalPrice == null || additionalPrice.signum() < 0) {
      throw new IllegalArgumentException("추가금은 0 이상이어야 합니다.");
    }
    this.additionalPrice = additionalPrice;
    this.stock = stock;
    this.status = status;
  }

  void assignProduct(Product product) {
    this.product = product;
  }

  public void decreaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 양수여야 합니다.");
    }
    if (this.stock < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_OPTION_COMBINATION);
    }
    this.stock -= quantity;
  }

  public void increaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 양수여야 합니다.");
    }
    this.stock += quantity;
  }
}
