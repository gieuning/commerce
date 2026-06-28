package com.gieun.commerce.domain.payment.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCancelResult {

  String paymentKey;
  String pgStatus;
  String pgCancellationKey;
  BigDecimal cancelAmount;
  LocalDateTime cancelledAt;
  String requestPayload;
  String responsePayload;
}
