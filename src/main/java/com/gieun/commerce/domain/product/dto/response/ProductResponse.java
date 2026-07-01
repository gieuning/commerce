package com.gieun.commerce.domain.product.dto.response;

import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

  Long id;
  String name;
  String description;
  BigDecimal price;
  int stock;
  ProductStatus status;
  boolean soldOut;
  String imageUrl;
  LocalDateTime createdAt;

  public static ProductResponse from(Product product, boolean soldOut) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stock(product.getStock())
        .status(product.getStatus())
        .soldOut(soldOut)
        .imageUrl(product.getImageUrl())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
