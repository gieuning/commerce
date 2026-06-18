package com.gieun.commerce.domain.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ProductCreateRequest {

  @NotBlank
  String name;

  String description;

  @NotNull
  @Positive
  BigDecimal price;

  @PositiveOrZero
  Integer stock;

  String imageUrl;

  List<@Valid @NotNull OptionGroupRequest> optionGroups;
  List<@Valid @NotNull OptionCombinationRequest> combinations;
}
