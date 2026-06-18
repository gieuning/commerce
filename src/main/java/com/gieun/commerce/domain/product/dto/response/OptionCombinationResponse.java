package com.gieun.commerce.domain.product.dto.response;

import com.gieun.commerce.domain.product.entity.CombinationValue;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.OptionValue;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import java.math.BigDecimal;
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
public class OptionCombinationResponse {

  Long id;
  List<String> optionValues;
  BigDecimal additionalPrice;
  BigDecimal finalPrice;
  int stock;
  ProductStatus status;

  public static OptionCombinationResponse from(OptionCombination combination, BigDecimal basePrice) {
    return OptionCombinationResponse.builder()
        .id(combination.getId())
        .optionValues(combination.getValues().stream()
            .map(CombinationValue::getOptionValue)
            .map(OptionValue::getName)
            .toList())
        .additionalPrice(combination.getAdditionalPrice())
        .finalPrice(basePrice.add(combination.getAdditionalPrice()))
        .stock(combination.getStock())
        .status(combination.getStatus())
        .build();
  }
}
