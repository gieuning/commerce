package com.gieun.commerce.domain.product.dto.request;

import com.gieun.commerce.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class OptionCombinationUpdateRequest {

  @NotNull
  @PositiveOrZero
  BigDecimal additionalPrice;

  @NotNull
  @PositiveOrZero
  Integer stock;

  @NotNull
  ProductStatus status;
}
