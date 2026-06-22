package com.gieun.commerce.domain.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gieun.commerce.domain.cart.entity.CartItem;
import com.gieun.commerce.domain.product.entity.CombinationValue;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.OptionValue;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

  Long itemId;
  Long productId;
  String productName;
  String imageUrl;
  Long optionCombinationId;
  List<String> optionValues;
  BigDecimal unitPrice;
  int quantity;
  BigDecimal subtotal;
  int stock;
  boolean available;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  String unavailableReason;

  public static CartItemResponse of(CartItem item, Product product, OptionCombination combination) {
    boolean productMissing = product == null;
    boolean optionMissing = item.getOptionCombinationId() != null && combination == null;

    boolean priceCalculable = !productMissing && !optionMissing;

    BigDecimal basePrice = priceCalculable ? product.getPrice() : BigDecimal.ZERO;
    BigDecimal additionalPrice =
        priceCalculable && combination != null ? combination.getAdditionalPrice() : BigDecimal.ZERO;
    BigDecimal unitPrice = basePrice.add(additionalPrice);

    int stock = resolveStock(productMissing, product, combination);

    boolean saleable = !productMissing
        && !optionMissing
        && product.getStatus() == ProductStatus.FOR_SALE
        && (combination == null || combination.getStatus() == ProductStatus.FOR_SALE);
    boolean available = saleable && stock >= item.getQuantity();
    String unavailableReason = resolveUnavailableReason(
        productMissing, optionMissing, product, combination, stock, item.getQuantity());

    List<String> optionValues = combination == null
        ? List.of()
        : combination.getValues().stream()
            .map(CombinationValue::getOptionValue)
            .map(OptionValue::getName)
            .toList();

    return CartItemResponse.builder()
        .itemId(item.getId())
        .productId(item.getProductId())
        .productName(productMissing ? null : product.getName())
        .imageUrl(productMissing ? null : product.getImageUrl())
        .optionCombinationId(item.getOptionCombinationId())
        .optionValues(optionValues)
        .unitPrice(unitPrice)
        .quantity(item.getQuantity())
        .subtotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
        .stock(stock)
        .available(available)
        .unavailableReason(unavailableReason)
        .build();
  }

  private static String resolveUnavailableReason(boolean productMissing, boolean optionMissing,
      Product product, OptionCombination combination, int stock, int quantity) {
    if (productMissing) {
      return "PRODUCT_NOT_FOUND";
    }
    if (optionMissing) {
      return "OPTION_NOT_FOUND";
    }
    if (product.getStatus() != ProductStatus.FOR_SALE) {
      return "PRODUCT_NOT_FOR_SALE";
    }
    if (combination != null && combination.getStatus() != ProductStatus.FOR_SALE) {
      return "OPTION_NOT_FOR_SALE";
    }
    if (stock < quantity) {
      return "OUT_OF_STOCK";
    }
    return null;
  }

  private static int resolveStock(boolean productMissing, Product product,
      OptionCombination combination) {
    if (combination != null) {
      return combination.getStock();
    }
    if (productMissing) {
      return 0;
    }
    return product.getStock();
  }

}
