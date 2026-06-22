package com.gieun.commerce.domain.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemAddRequest {

  @NotNull
  @Positive
  Long productId;

  @Positive
  Long optionCombinationId;

  @NotNull
  @Positive
  Integer quantity;

}
