package com.gieun.commerce.domain.payment.dto.response;

import com.gieun.commerce.domain.payment.entity.PaymentCancellation;
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
public class PaymentCancellationResponse {

  Long paymentCancellationId;
  Long paymentId;
  PgProvider pgProvider;
  String paymentKey;
  String pgCancellationKey;
  BigDecimal cancelAmount;
  String cancelReason;
  LocalDateTime cancelledAt;

  public static PaymentCancellationResponse of(PaymentCancellation cancellation) {
    return PaymentCancellationResponse.builder()
        .paymentCancellationId(cancellation.getId())
        .paymentId(cancellation.getPaymentId())
        .pgProvider(cancellation.getPgProvider())
        .paymentKey(cancellation.getPaymentKey())
        .pgCancellationKey(cancellation.getPgCancellationKey())
        .cancelAmount(cancellation.getCancelAmount())
        .cancelReason(cancellation.getCancelReason())
        .cancelledAt(cancellation.getCancelledAt())
        .build();
  }
}
