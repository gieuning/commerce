package com.gieun.commerce.domain.payment.dto.response;

import com.gieun.commerce.domain.payment.entity.Payment;
import com.gieun.commerce.domain.payment.entity.PaymentMethod;
import com.gieun.commerce.domain.payment.entity.PaymentStatus;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

  Long paymentId;
  Long orderId;
  Long userId;
  String paymentKey;
  String merchantOrderId;
  PgProvider pgProvider;
  PaymentMethod method;
  PaymentStatus status;
  BigDecimal amount;
  LocalDateTime approvedAt;
  LocalDateTime cancelledAt;
  String failureCode;
  String failureMessage;

  public static PaymentResponse of(Payment payment) {
    return PaymentResponse.builder()
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .userId(payment.getUserId())
        .paymentKey(payment.getPaymentKey())
        .merchantOrderId(payment.getMerchantOrderId())
        .pgProvider(payment.getPgProvider())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .amount(payment.getAmount())
        .approvedAt(payment.getApprovedAt())
        .cancelledAt(payment.getCancelledAt())
        .failureCode(payment.getFailureCode())
        .failureMessage(payment.getFailureMessage())
        .build();
  }
}
