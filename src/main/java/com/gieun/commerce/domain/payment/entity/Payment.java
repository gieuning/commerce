package com.gieun.commerce.domain.payment.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
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
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  Long orderId;

  @Column(nullable = false)
  Long userId;

  @Column(unique = true)
  String paymentKey;

  @Column(nullable = false, unique = true)
  String merchantOrderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PgProvider pgProvider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PaymentStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal amount;

  LocalDateTime approvedAt;

  LocalDateTime cancelledAt;

  String failureCode;

  @Column(columnDefinition = "TEXT")
  String failureMessage;

  public static Payment request(
      Long orderId,
      Long userId,
      String merchantOrderId,
      PgProvider pgProvider,
      PaymentMethod method,
      BigDecimal amount
  ) {
    validateAmount(amount);

    return Payment.builder()
        .orderId(Objects.requireNonNull(orderId, "주문 ID는 필수입니다."))
        .userId(Objects.requireNonNull(userId, "회원 ID는 필수입니다."))
        .merchantOrderId(Objects.requireNonNull(merchantOrderId, "주문 번호는 필수입니다."))
        .pgProvider(Objects.requireNonNull(pgProvider, "PG사는 필수입니다."))
        .method(Objects.requireNonNull(method, "결제 수단은 필수입니다."))
        .status(PaymentStatus.REQUESTED)
        .amount(amount)
        .build();
  }

  public void approve(String paymentKey, LocalDateTime approvedAt) {
    validateStatus(PaymentStatus.REQUESTED, DomainExceptionCode.CANNOT_CONFIRM_PAYMENT);
    this.paymentKey = Objects.requireNonNull(paymentKey, "PG 결제 키는 필수입니다.");
    this.approvedAt = Objects.requireNonNull(approvedAt, "결제 승인 시간은 필수입니다.");
    this.status = PaymentStatus.APPROVED;
  }

  public void fail(String failureCode, String failureMessage) {
    validateStatus(PaymentStatus.REQUESTED, DomainExceptionCode.CANNOT_CONFIRM_PAYMENT);
    this.failureCode = failureCode;
    this.failureMessage = failureMessage;
    this.status = PaymentStatus.FAILED;
  }

  public void cancel(LocalDateTime cancelledAt) {
    validateStatus(PaymentStatus.APPROVED, DomainExceptionCode.CANNOT_CANCEL_PAYMENT);
    this.cancelledAt = Objects.requireNonNull(cancelledAt, "결제 취소 시간은 필수입니다.");
    this.status = PaymentStatus.CANCELLED;
  }

  private void validateStatus(PaymentStatus expectedStatus, DomainExceptionCode exceptionCode) {
    if (status != expectedStatus) {
      throw new DomainException(exceptionCode);
    }
  }

  private static void validateAmount(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new DomainException(DomainExceptionCode.INVALID_PAYMENT_AMOUNT);
    }
  }
}
