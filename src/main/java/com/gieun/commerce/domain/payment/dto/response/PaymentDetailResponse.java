package com.gieun.commerce.domain.payment.dto.response;

import com.gieun.commerce.domain.payment.entity.Payment;
import com.gieun.commerce.domain.payment.entity.PaymentCancellation;
import com.gieun.commerce.domain.payment.entity.PaymentEvent;
import com.gieun.commerce.domain.payment.entity.PaymentReceipt;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDetailResponse {

  PaymentResponse payment;
  PaymentReceiptResponse receipt;
  List<PaymentEventResponse> events;
  List<PaymentCancellationResponse> cancellations;

  public static PaymentDetailResponse of(
      Payment payment,
      PaymentReceipt receipt,
      List<PaymentEvent> events,
      List<PaymentCancellation> cancellations
  ) {
    return PaymentDetailResponse.builder()
        .payment(PaymentResponse.of(payment))
        .receipt(receipt == null ? null : PaymentReceiptResponse.of(receipt))
        .events(events.stream()
            .map(PaymentEventResponse::of)
            .toList())
        .cancellations(cancellations.stream()
            .map(PaymentCancellationResponse::of)
            .toList())
        .build();
  }
}
