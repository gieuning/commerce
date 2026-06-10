package com.gieun.commerce.domain.product.dto.response;

import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

  Long id;
  String name;
  String description;
  BigDecimal price;
  int stock;
  ProductStatus status;
  String imageUrl;
  LocalDateTime createdAt;

  public static ProductResponse from(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stock(product.getStock())
        .status(product.getStatus())
        .imageUrl(product.getImageUrl())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
