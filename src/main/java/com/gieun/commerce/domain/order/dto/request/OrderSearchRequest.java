package com.gieun.commerce.domain.order.dto.request;

import com.gieun.commerce.domain.order.entity.OrderStatus;
import com.gieun.commerce.domain.order.repository.condition.OrderSearchCondition;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSearchRequest {

  String productName;

  OrderStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate from;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate to;

  public OrderSearchCondition toCondition() {
    return OrderSearchCondition.builder()
        .productName(productName)
        .status(status)
        .from(from)
        .to(to)
        .build();
  }
}
