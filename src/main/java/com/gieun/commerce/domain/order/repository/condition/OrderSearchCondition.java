package com.gieun.commerce.domain.order.repository.condition;

import com.gieun.commerce.domain.order.entity.OrderStatus;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSearchCondition {

  String productName;
  OrderStatus status;
  LocalDate from;
  LocalDate to;
}
