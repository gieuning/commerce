package com.gieun.commerce.domain.product.dto.response;

import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class ProductDetailResponse {

  Long id;
  String name;
  String description;
  BigDecimal price;
  int stock;
  ProductStatus status;
  boolean soldOut;
  String imageUrl;
  LocalDateTime createdAt;
  boolean hasOptions;
  List<OptionGroupResponse> optionGroups;
  List<OptionCombinationResponse> combinations;

  public static ProductDetailResponse from(Product product) {
    return ProductDetailResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stock(product.getStock())
        .status(product.getStatus())
        .soldOut(product.isSoldOut())
        .imageUrl(product.getImageUrl())
        .createdAt(product.getCreatedAt())
        .hasOptions(product.hasOptions())
        .optionGroups(product.getOptionGroups().stream()
            .map(OptionGroupResponse::from)
            .toList())
        .combinations(product.getOptionCombinations().stream()
            .map(combination -> OptionCombinationResponse.from(combination, product.getPrice()))
            .toList())
        .build();
  }
}
