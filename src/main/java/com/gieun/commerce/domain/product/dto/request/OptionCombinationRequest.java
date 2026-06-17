package com.gieun.commerce.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionCombinationRequest {

  @NotEmpty
  List<@NotBlank String> optionValues;

  @PositiveOrZero
  BigDecimal additionalPrice;

  @NotNull
  @PositiveOrZero
  Integer stock;
}
