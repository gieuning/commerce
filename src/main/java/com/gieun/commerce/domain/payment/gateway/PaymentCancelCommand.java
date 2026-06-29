package com.gieun.commerce.domain.payment.gateway;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCancelCommand {

  String paymentKey;
  BigDecimal cancelAmount;
  String cancelReason;
}
