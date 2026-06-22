package com.gieun.commerce.domain.cart.dto.response;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartSummary {

  int totalQuantity;
  BigDecimal totalPrice;

  public static CartSummary empty() {
    return CartSummary.builder()
        .totalQuantity(0)
        .totalPrice(BigDecimal.ZERO)
        .build();
  }
}
