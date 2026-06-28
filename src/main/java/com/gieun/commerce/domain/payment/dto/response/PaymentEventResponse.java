package com.gieun.commerce.domain.payment.dto.response;

import com.gieun.commerce.domain.payment.entity.PaymentEvent;
import com.gieun.commerce.domain.payment.entity.PaymentEventType;
import com.gieun.commerce.domain.payment.entity.PaymentStatus;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEventResponse {

  Long paymentEventId;
  Long paymentId;
  PaymentEventType eventType;
  PaymentStatus paymentStatus;
  PgProvider pgProvider;
  String pgEventId;
  String requestPayload;
  String responsePayload;
  String failureCode;
  String failureMessage;
  LocalDateTime occurredAt;

  public static PaymentEventResponse of(PaymentEvent event) {
    return PaymentEventResponse.builder()
        .paymentEventId(event.getId())
        .paymentId(event.getPaymentId())
        .eventType(event.getEventType())
        .paymentStatus(event.getPaymentStatus())
        .pgProvider(event.getPgProvider())
        .pgEventId(event.getPgEventId())
        .requestPayload(event.getRequestPayload())
        .responsePayload(event.getResponsePayload())
        .failureCode(event.getFailureCode())
        .failureMessage(event.getFailureMessage())
        .occurredAt(event.getOccurredAt())
        .build();
  }
}
