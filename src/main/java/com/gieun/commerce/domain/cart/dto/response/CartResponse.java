package com.gieun.commerce.domain.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {

  List<CartItemResponse> items;
  int totalQuantity;
  BigDecimal totalPrice;

  public static CartResponse of(List<CartItemResponse> items, CartSummary summary) {
    return CartResponse.builder()
        .items(items)
        .totalQuantity(summary.getTotalQuantity())
        .totalPrice(summary.getTotalPrice())
        .build();
  }

  public static CartResponse empty() {
    return of(List.of(), CartSummary.empty());
  }


}
