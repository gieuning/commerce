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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_cancellations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCancellation extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  Long paymentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PgProvider pgProvider;

  @Column(nullable = false)
  String paymentKey;

  String pgCancellationKey;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal cancelAmount;

  @Column(nullable = false)
  String cancelReason;

  @Column(nullable = false)
  LocalDateTime cancelledAt;

  @Column(columnDefinition = "TEXT")
  String requestPayload;

  @Column(columnDefinition = "TEXT")
  String responsePayload;

  public static PaymentCancellation of(CreateCommand command) {
    Objects.requireNonNull(command, "결제 취소 생성 정보는 필수입니다.");
    validateCancelAmount(command.cancelAmount);

    return PaymentCancellation.builder()
        .paymentId(Objects.requireNonNull(command.paymentId, "결제 ID는 필수입니다."))
        .pgProvider(Objects.requireNonNull(command.pgProvider, "PG사는 필수입니다."))
        .paymentKey(Objects.requireNonNull(command.paymentKey, "PG 결제 키는 필수입니다."))
        .pgCancellationKey(command.pgCancellationKey)
        .cancelAmount(command.cancelAmount)
        .cancelReason(Objects.requireNonNull(command.cancelReason, "취소 사유는 필수입니다."))
        .cancelledAt(Objects.requireNonNull(command.cancelledAt, "결제 취소 시간은 필수입니다."))
        .requestPayload(command.requestPayload)
        .responsePayload(command.responsePayload)
        .build();
  }

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class CreateCommand {

    Long paymentId;
    PgProvider pgProvider;
    String paymentKey;
    String pgCancellationKey;
    BigDecimal cancelAmount;
    String cancelReason;
    LocalDateTime cancelledAt;
    String requestPayload;
    String responsePayload;
  }

  private static void validateCancelAmount(BigDecimal cancelAmount) {
    Objects.requireNonNull(cancelAmount, "취소 금액은 필수입니다.");
    if (cancelAmount.signum() <= 0) {
      throw new IllegalArgumentException("취소 금액은 0보다 커야 합니다.");
    }
  }
}
