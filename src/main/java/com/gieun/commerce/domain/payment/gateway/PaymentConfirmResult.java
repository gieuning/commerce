package com.gieun.commerce.domain.payment.gateway;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmResult {

  String paymentKey;
  String merchantOrderId;
  String pgStatus;
  BigDecimal totalAmount;
  BigDecimal suppliedAmount;
  BigDecimal vat;
  String receiptUrl;
  OffsetDateTime approvedAt;
  String requestPayload;
  String responsePayload;
}
