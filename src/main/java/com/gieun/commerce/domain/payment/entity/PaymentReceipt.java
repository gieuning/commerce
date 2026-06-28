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
@Table(name = "payment_receipts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentReceipt extends BaseEntity {

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

  @Column(nullable = false)
  String receiptUrl;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal totalAmount;

  @Column(precision = 10, scale = 2)
  BigDecimal suppliedAmount;

  @Column(precision = 10, scale = 2)
  BigDecimal vat;

  LocalDateTime issuedAt;

  @Column(columnDefinition = "TEXT")
  String rawPayload;

  public static PaymentReceipt issue(IssueCommand command) {
    Objects.requireNonNull(command, "영수증 발급 정보는 필수입니다.");
    validateTotalAmount(command.totalAmount);

    return PaymentReceipt.builder()
        .paymentId(Objects.requireNonNull(command.paymentId, "결제 ID는 필수입니다."))
        .pgProvider(Objects.requireNonNull(command.pgProvider, "PG사는 필수입니다."))
        .paymentKey(Objects.requireNonNull(command.paymentKey, "PG 결제 키는 필수입니다."))
        .receiptUrl(Objects.requireNonNull(command.receiptUrl, "영수증 URL은 필수입니다."))
        .totalAmount(command.totalAmount)
        .suppliedAmount(command.suppliedAmount)
        .vat(command.vat)
        .issuedAt(command.issuedAt)
        .rawPayload(command.rawPayload)
        .build();
  }

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class IssueCommand {

    Long paymentId;
    PgProvider pgProvider;
    String paymentKey;
    String receiptUrl;
    BigDecimal totalAmount;
    BigDecimal suppliedAmount;
    BigDecimal vat;
    LocalDateTime issuedAt;
    String rawPayload;
  }

  private static void validateTotalAmount(BigDecimal totalAmount) {
    Objects.requireNonNull(totalAmount, "영수증 총 금액은 필수입니다.");
    if (totalAmount.signum() <= 0) {
      throw new IllegalArgumentException("영수증 총 금액은 0보다 커야 합니다.");
    }
  }
}
