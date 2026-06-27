package com.gieun.commerce.domain.order.dto.response;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

  Long orderId;
  OrderStatus status;
  BigDecimal totalProductPrice;
  BigDecimal discountAmount;
  BigDecimal shippingFee;
  BigDecimal totalPrice;
  LocalDateTime orderedAt;
  List<OrderItemResponse> items;

  public static OrderResponse of(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .status(order.getStatus())
        .totalProductPrice(order.getTotalProductPrice())
        .discountAmount(order.getDiscountAmount())
        .shippingFee(order.getShippingFee())
        .totalPrice(order.getTotalPrice())
        .orderedAt(order.getOrderedAt())
        .items(order.getItems().stream()
            .map(OrderItemResponse::of)
            .toList())
        .build();
  }
}
