package com.gieun.commerce.domain.payment.entity;

import com.gieun.commerce.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEvent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  Long paymentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PaymentEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PgProvider pgProvider;

  String pgEventId;

  @Column(columnDefinition = "TEXT")
  String requestPayload;

  @Column(columnDefinition = "TEXT")
  String responsePayload;

  String failureCode;

  @Column(columnDefinition = "TEXT")
  String failureMessage;

  @Column(nullable = false)
  LocalDateTime occurredAt;

  public static PaymentEvent of(CreateCommand command) {
    Objects.requireNonNull(command, "결제 이벤트 생성 정보는 필수입니다.");

    return PaymentEvent.builder()
        .paymentId(Objects.requireNonNull(command.paymentId, "결제 ID는 필수입니다."))
        .eventType(Objects.requireNonNull(command.eventType, "결제 이벤트 타입은 필수입니다."))
        .paymentStatus(Objects.requireNonNull(command.paymentStatus, "결제 상태는 필수입니다."))
        .pgProvider(Objects.requireNonNull(command.pgProvider, "PG사는 필수입니다."))
        .pgEventId(command.pgEventId)
        .requestPayload(command.requestPayload)
        .responsePayload(command.responsePayload)
        .failureCode(command.failureCode)
        .failureMessage(command.failureMessage)
        .occurredAt(Objects.requireNonNull(command.occurredAt, "이벤트 발생 시간은 필수입니다."))
        .build();
  }

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class CreateCommand {

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
  }
}
