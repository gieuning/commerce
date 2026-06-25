package com.gieun.commerce.domain.order.dto.response;

import com.gieun.commerce.domain.order.entity.OrderItem;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {

  Long itemId;
  Long productId;
  Long optionCombinationId;
  String productName;
  String optionValues;
  BigDecimal unitPrice;
  int quantity;
  BigDecimal subtotal;

  public static OrderItemResponse of(OrderItem item) {
    return OrderItemResponse.builder()
        .itemId(item.getId())
        .productId(item.getProductId())
        .optionCombinationId(item.getOptionCombinationId())
        .productName(item.getProductName())
        .optionValues(item.getOptionValues())
        .unitPrice(item.getUnitPrice())
        .quantity(item.getQuantity())
        .subtotal(item.getSubtotal())
        .build();
  }
}
