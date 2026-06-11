package com.gieun.commerce.domain.product.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  String name;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal price;

  @Column(nullable = false)
  int stock;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  ProductStatus status;

  String imageUrl;

  public static Product create(String name, String description, BigDecimal price, int stock,
      String imageUrl) {
    return Product.builder()
        .name(name)
        .description(description)
        .price(price)
        .stock(stock)
        .status(ProductStatus.FOR_SALE)
        .imageUrl(imageUrl)
        .build();
  }

  public void update(String name, String description, BigDecimal price, String imageUrl) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.imageUrl = imageUrl;
  }

  public void updateStock(int stock) {
    if (stock < 0) {
      throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
    }
    this.stock = stock;
  }

  public void decreaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 양수여야 합니다.");
    }
    if (this.stock < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT);
    }
    this.stock -= quantity;
  }

  public void increaseStock(int quantity) {
    this.stock += quantity;
  }

  public void discontinue() {
    this.status = ProductStatus.STOP_SALE;
  }
}
