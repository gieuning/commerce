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

/**
 * 결제는 PG에서 승인(결제)됐으나 로컬 검증/재고 차감 실패로 되돌려야 하는데, 그 보상 취소(환불)마저 실패한 건을 기록하는 아웃박스.
 * 스케줄러가 PENDING 건을 폴링하여 환불을 재시도한다.
 */
@Entity
@Table(name = "payment_compensations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCompensation extends BaseEntity {

  public static final int DEFAULT_MAX_ATTEMPTS = 10;
  private static final long BASE_RETRY_DELAY_SECONDS = 60L;
  private static final long MAX_RETRY_DELAY_SECONDS = 3600L;

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

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal cancelAmount;

  @Column(nullable = false)
  String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  CompensationStatus status;

  @Column(nullable = false)
  int attemptCount;

  @Column(nullable = false)
  int maxAttempts;

  LocalDateTime nextRetryAt;

  @Column(columnDefinition = "TEXT")
  String lastError;

  public static PaymentCompensation of(CreateCommand command) {
    Objects.requireNonNull(command, "보상 생성 정보는 필수입니다.");
    validateCancelAmount(command.cancelAmount);

    return PaymentCompensation.builder()
        .paymentId(Objects.requireNonNull(command.paymentId, "결제 ID는 필수입니다."))
        .pgProvider(Objects.requireNonNull(command.pgProvider, "PG사는 필수입니다."))
        .paymentKey(Objects.requireNonNull(command.paymentKey, "PG 결제 키는 필수입니다."))
        .cancelAmount(command.cancelAmount)
        .reason(Objects.requireNonNull(command.reason, "보상 사유는 필수입니다."))
        .status(Objects.requireNonNull(command.status, "보상 상태는 필수입니다."))
        .attemptCount(command.attemptCount)
        .maxAttempts(command.maxAttempts <= 0 ? DEFAULT_MAX_ATTEMPTS : command.maxAttempts)
        .nextRetryAt(command.nextRetryAt)
        .lastError(command.lastError)
        .build();
  }

  /** 환불 재시도 성공 처리. */
  public void markDone() {
    this.status = CompensationStatus.DONE;
    this.nextRetryAt = null;
  }

  /** 환불 재시도 실패 처리. 최대 횟수 초과 시 GAVE_UP(수동 처리)으로 전환. */
  public void recordFailure(String error, LocalDateTime from) {
    this.attemptCount += 1;
    this.lastError = error;

    if (attemptCount >= maxAttempts) {
      this.status = CompensationStatus.GAVE_UP;
      this.nextRetryAt = null;
      return;
    }

    this.status = CompensationStatus.PENDING;
    this.nextRetryAt = nextRetryAt(attemptCount, from);
  }

  /** 지수 백오프(상한 있음): 60s, 120s, 240s ... 최대 3600s. */
  public static LocalDateTime nextRetryAt(int attemptCount, LocalDateTime from) {
    int shift = Math.min(Math.max(attemptCount - 1, 0), 16);
    long delay = Math.min(BASE_RETRY_DELAY_SECONDS << shift, MAX_RETRY_DELAY_SECONDS);
    return from.plusSeconds(delay);
  }

  private static void validateCancelAmount(BigDecimal cancelAmount) {
    Objects.requireNonNull(cancelAmount, "환불 금액은 필수입니다.");
    if (cancelAmount.signum() <= 0) {
      throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
    }
  }

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class CreateCommand {

    Long paymentId;
    PgProvider pgProvider;
    String paymentKey;
    BigDecimal cancelAmount;
    String reason;
    CompensationStatus status;
    int attemptCount;
    int maxAttempts;
    LocalDateTime nextRetryAt;
    String lastError;
  }
}
